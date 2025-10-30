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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public final class DataService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private static final short MAX_SAVE_RETRIES = 5; // Used only for custom URL collisions
    private final Random random = new SecureRandom(); // Create one random instance

    @Autowired
    private final DataRepository dataRepository;

    @Autowired
    private final CacheService cacheService;

    @Value("${features.cache.enabled}")
    private boolean cacheEnabled;

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    @Value("${features.async.post.enabled}")
    private boolean asyncPostEnabled;

    public String findOrigin(String shortenedUrl) {
        // Find info from cache first
        if (cacheEnabled) {
            String cachedUrl = cacheService.getOriginalUrlFromCache(shortenedUrl);
            if (cachedUrl != null){
                return cachedUrl;
            }
        }

        Data data = dataRepository.findByShortenedUrl(shortenedUrl).orElse(null);
        if (data != null) {
            // Check if the URL has expired
            // URL is expired if expirationTime is not null and is before the current time
            // If expirationTime is null, the URL is not expired
            if (data.getExpirationTime() == null ||
                    data.getExpirationTime().isAfter(LocalDateTime.now())) {                

                if (cacheEnabled) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime expTime = data.getExpirationTime();
                    cacheService.saveToCache(shortenedUrl, data.getUrl(), expTime != null ?
                            Duration.between(expTime, now).toSeconds() : null);
                }
                return data.getUrl();
            } else {
                deleteUrl(shortenedUrl);
                return null;
            }
        }
        
        return null;
    }

    private Data generateShortenedUrlAndSave(Data data) {
        Random random = new SecureRandom();
        for (int i = 0; i < MAX_SAVE_RETRIES; i++) {
            try {
                String shortenedUrl = CustomUUID.random(random);
                data.setShortenedUrl(shortenedUrl);
                return dataRepository.save(data);
            } catch (Exception ignored) {
            }
        }
        return null; // If all retries fail, return null
    }

    public String shortenUrl(CreationRequest request) throws SQLIntegrityConstraintViolationException {
        System.err.println(request);
        String url = request.getUrl();
        Long ttlMinute = request.getTtlMinute();
        String customShortenedUrl = request.getCustomShortenedUrl();

        LocalDateTime creationTime = LocalDateTime.now();
        LocalDateTime expirationTime = ttlMinute == null ?
                null : creationTime.plusMinutes(ttlMinute);

        Data data = Data.builder()
                .url(url)
                .creationTime(creationTime)
                .expirationTime(expirationTime)
                .clickCount(0)
                .build();
        
        String shortenedUrl;
        
        if (customShortenedUrl != null) {
            // --- SLOW PATH (Synchronous) ---
            // We must do this synchronously to check for 409 Conflict
            log.debug("Using synchronous path for custom URL: {}", customShortenedUrl);
            try {
                data.setShortenedUrl(customShortenedUrl.trim());
                dataRepository.save(data);
                shortenedUrl = data.getShortenedUrl();
            } catch (Exception e) {
                // This exception is caught by the controller
                throw new SQLIntegrityConstraintViolationException("Custom shortened URL already exists.");
            }
        } else if (asyncPostEnabled) {
            // --- PATH 2: ASYNC POST FLAG is ON (Fast Path) ---
            // Generate a random URL, publish to RMQ
            log.debug("Using asynchronous path for random URL");
            shortenedUrl = CustomUUID.random(random);
            data.setShortenedUrl(shortenedUrl);

            // 1. Publish to RabbitMQ for DB persistence            
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
                // If RabbitMQ fails, we must not return a link that will never be saved.
                log.error("Failed to publish link creation event for {}: {}", shortenedUrl, e.getMessage());
                // Fallback to synchronous save
                log.warn("Falling back to synchronous save for {}", shortenedUrl);
                Data savedData = generateShortenedUrlAndSave(data);
                if (savedData == null) {
                    throw new SQLIntegrityConstraintViolationException("Failed to generate a unique shortened URL.");
                }
                shortenedUrl = savedData.getShortenedUrl();
            }
            
        } else {
            // --- PATH 3: ASYNC POST FLAG is OFF (Sync Fallback) ---
            log.warn("Using synchronous fallback for random URL creation");
            Data savedData = generateShortenedUrlAndSave(data);
            if (savedData == null) {
                throw new SQLIntegrityConstraintViolationException("Failed to generate a unique shortened URL.");
            }
            shortenedUrl = savedData.getShortenedUrl();
        }

        if (cacheEnabled) {
            cacheService.saveToCache(shortenedUrl, url, ttlMinute != null ? ttlMinute * 60 : null);
        }
        return shortenedUrl;
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
