package com.example.linkshortener.service;

import com.example.linkshortener.config.RabbitMQConfig;
import com.example.linkshortener.data.dto.CreationRequest;
import com.example.linkshortener.data.dto.LinkCreationEvent;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import com.example.linkshortener.util.CustomUUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CreationService {

    private static final Logger log = LoggerFactory.getLogger(CreationService.class);
    private static final short MAX_SAVE_RETRIES = 5;
    private final Random random = new SecureRandom();

    private final DataRepository dataRepository;
    private final CreationCacheService cacheService; // Using the specialized cache service
    private final RabbitTemplate rabbitTemplate;

    @Value("${features.cache.enabled}")
    private boolean cacheEnabled;

    @Value("${features.async.post.enabled}")
    private boolean asyncPostEnabled;

    public String shortenUrl(CreationRequest request) throws SQLIntegrityConstraintViolationException {
        String url = request.getUrl();
        Long ttlMinute = request.getTtlMinute();
        String customShortenedUrl = request.getCustomShortenedUrl();

        LocalDateTime creationTime = LocalDateTime.now();
        LocalDateTime expirationTime = ttlMinute == null ? null : creationTime.plusMinutes(ttlMinute);

        Data data = Data.builder()
                .url(url)
                .creationTime(creationTime)
                .expirationTime(expirationTime)
                .clickCount(0)
                .build();
        
        String shortenedUrl;
        
        if (customShortenedUrl != null) {
            // --- SYNC PATH: Custom URLs check DB directly for collisions ---
            log.debug("Using synchronous path for custom URL: {}", customShortenedUrl);
            try {
                data.setShortenedUrl(customShortenedUrl.trim());
                dataRepository.save(data);
                shortenedUrl = data.getShortenedUrl();
            } catch (Exception e) {
                throw new SQLIntegrityConstraintViolationException("Custom shortened URL already exists.");
            }
        } else if (asyncPostEnabled) {
            // --- ASYNC PATH: Random URL -> RabbitMQ ---
            log.debug("Using asynchronous path for random URL");
            shortenedUrl = CustomUUID.random(random);
            data.setShortenedUrl(shortenedUrl);
     
            try {
                LinkCreationEvent event = LinkCreationEvent.builder()
                        .url(data.getUrl())
                        .shortenedUrl(data.getShortenedUrl())
                        .creationTime(data.getCreationTime())
                        .expirationTime(data.getExpirationTime())
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.LINK_CREATION_ROUTING_KEY,
                        event
                );
            } catch (Exception e) {
                log.error("Failed to publish event, falling back to sync save: {}", e.getMessage());
                Data savedData = generateShortenedUrlAndSave(data);
                if (savedData == null) {
                    throw new SQLIntegrityConstraintViolationException("Failed to generate unique URL.");
                }
                shortenedUrl = savedData.getShortenedUrl();
            }
            
        } else {
            // --- SYNC FALLBACK ---
            Data savedData = generateShortenedUrlAndSave(data);
            if (savedData == null) throw new SQLIntegrityConstraintViolationException("Failed to generate unique URL.");
            shortenedUrl = savedData.getShortenedUrl();
        }

        // NEW REQUIREMENT: Creation Service writes to Cache immediately
        if (cacheEnabled) {
            cacheService.saveToCache(shortenedUrl, url, ttlMinute != null ? ttlMinute * 60 : null);
        }
        return shortenedUrl;
    }

    private Data generateShortenedUrlAndSave(Data data) {
        for (int i = 0; i < MAX_SAVE_RETRIES; i++) {
            try {
                String shortenedUrl = CustomUUID.random(random);
                data.setShortenedUrl(shortenedUrl);
                return dataRepository.save(data);
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void deleteUrl(String shortenedUrl) {
        dataRepository.findByShortenedUrl(shortenedUrl)
                .ifPresent(dataRepository::delete); // Shorter lambda

        cacheService.deleteFromCache(shortenedUrl); // Still delete from Redis just in case
    }

    public void deleteAll() {
        dataRepository.deleteAll();
    }

    public List<Data> findAll(int page, int size) {
        return dataRepository.findAll(PageRequest.of(page, size)).getContent();
    }
}