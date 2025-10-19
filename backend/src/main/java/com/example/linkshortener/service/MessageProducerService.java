package com.example.linkshortener.service;

import com.example.linkshortener.config.RabbitMQConfig;
import com.example.linkshortener.data.message.UrlShorteningMessage;
import com.example.linkshortener.data.message.ClickTrackingMessage;
import com.example.linkshortener.data.message.CacheSyncMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducerService {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MessageProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUrlShorteningMessage(UrlShorteningMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.URL_SHORTENING_EXCHANGE,
            RabbitMQConfig.URL_SHORTENING_ROUTING_KEY,
            message
        );
    }

    public void sendClickTrackingMessage(ClickTrackingMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.CLICK_TRACKING_EXCHANGE,
            RabbitMQConfig.CLICK_TRACKING_ROUTING_KEY,
            message
        );
    }

    public void sendCacheSyncMessage(CacheSyncMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.CACHE_SYNC_EXCHANGE,
            RabbitMQConfig.CACHE_SYNC_ROUTING_KEY,
            message
        );
    }
}