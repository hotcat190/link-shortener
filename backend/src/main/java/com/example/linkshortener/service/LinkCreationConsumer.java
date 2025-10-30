package com.example.linkshortener.service;

import com.example.linkshortener.config.RabbitMQConfig;
import com.example.linkshortener.data.dto.LinkCreationEvent;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkCreationConsumer {

    private static final Logger log = LoggerFactory.getLogger(LinkCreationConsumer.class);

    @Autowired
    private DataRepository dataRepository;

    @RabbitListener(queues = RabbitMQConfig.LINK_CREATION_QUEUE)
    public void handleLinkCreationEvent(LinkCreationEvent event) {
        log.info("Received link creation event for: {}", event.getShortenedUrl());

        try {
            // Check if it somehow already exists (e.g., consumer retry)
            if (dataRepository.existsByShortenedUrl(event.getShortenedUrl())) {
                log.warn("Link {} already exists in DB. Skipping.", event.getShortenedUrl());
                return;
            }

            Data data = Data.builder()
                    .url(event.getUrl())
                    .shortenedUrl(event.getShortenedUrl())
                    .creationTime(event.getCreationTime())
                    .expirationTime(event.getExpirationTime())
                    .clickCount(0) // New links always start at 0
                    .build();

            dataRepository.save(data);
            log.info("Successfully saved {} to database.", data.getShortenedUrl());
            
        } catch (Exception e) {
            log.error("Error processing link creation event for {}: {}", event.getShortenedUrl(), e.getMessage());
            // This will cause the message to be re-queued, which is good.
            // After enough failures, RabbitMQ will move it to a Dead Letter Queue (if configured).
            throw e;
        }
    }
}