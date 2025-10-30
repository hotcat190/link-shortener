package com.example.linkshortener.service;

import com.example.linkshortener.config.RabbitMQConfig;
import com.example.linkshortener.data.dto.ClickEvent;
import com.example.linkshortener.data.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsConsumer {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsConsumer.class);

    @Autowired
    private DataRepository dataRepository;

    /**
     * Listens for click events and updates the click count in the database.
     */
    @RabbitListener(queues = RabbitMQConfig.CLICK_ANALYTICS_QUEUE)
    public void handleAnalyticsEvent(ClickEvent event) {
        log.info("Received click event for: {}", event.getShortenedUrl());
        
        try {
            dataRepository.findByShortenedUrl(event.getShortenedUrl()).ifPresent(data -> {
                data.setClickCount(data.getClickCount() + 1);
                dataRepository.save(data);
                log.info("Successfully updated click count for {}", data.getShortenedUrl());
            });
        } catch (Exception e) {
            log.error("Error processing click event for {}: {}", event.getShortenedUrl(), e.getMessage());
        }
    }
}
