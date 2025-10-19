package com.example.linkshortener.service;

import com.example.linkshortener.config.RabbitMQConfig;
import com.example.linkshortener.data.message.UrlShorteningMessage;
import com.example.linkshortener.data.message.ClickTrackingMessage;
import com.example.linkshortener.data.message.CacheSyncMessage;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageConsumerService {
    private final Logger logger = LoggerFactory.getLogger(MessageConsumerService.class);
    private final DataService dataService;
    private final DataRepository dataRepository;
    private final CacheService cacheService;

    public MessageConsumerService(DataService dataService, DataRepository dataRepository, CacheService cacheService) {
        this.dataService = dataService;
        this.dataRepository = dataRepository;
        this.cacheService = cacheService;
    }

    @RabbitListener(queues = RabbitMQConfig.URL_SHORTENING_QUEUE)
    public void handleUrlShortening(UrlShorteningMessage message) {
        try {
            Data data = Data.builder()
                    .url(message.getOriginalUrl())
                    .shortenedUrl(message.getCustomShortenedUrl())
                    .creationTime(LocalDateTime.now())
                    .expirationTime(message.getTtlMinute() != null ? 
                            LocalDateTime.now().plusMinutes(message.getTtlMinute()) : null)
                    .clickCount(0)
                    .build();

            dataRepository.save(data);
            
            if (message.getTtlMinute() != null) {
                cacheService.saveToCache(data.getShortenedUrl(), data.getUrl(), 
                        message.getTtlMinute() * 60);
            } else {
                cacheService.saveToCache(data.getShortenedUrl(), data.getUrl(), null);
            }
        } catch (Exception e) {
            logger.error("Error processing URL shortening message", e);
            throw e; // Will trigger retry mechanism
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CLICK_TRACKING_QUEUE)
    public void handleClickTracking(ClickTrackingMessage message) {
        try {
            cacheService.incrementClickCount(message.getShortenedUrl());
        } catch (Exception e) {
            logger.error("Error processing click tracking message", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CACHE_SYNC_QUEUE)
    public void handleCacheSync(CacheSyncMessage message) {
        try {
            dataRepository.findByShortenedUrl(message.getShortenedUrl())
                    .ifPresent(data -> {
                        data.setClickCount(data.getClickCount() + message.getClickCount());
                        dataRepository.save(data);
                    });
        } catch (Exception e) {
            logger.error("Error processing cache sync message", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(Object failedMessage) {
        logger.error("Message processing failed and moved to dead letter queue: {}", failedMessage);
        // Implement your dead letter handling logic here
        // You might want to store failed messages in a database or send notifications
    }
}