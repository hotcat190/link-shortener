package com.example.linkshortener.controller;

import com.example.linkshortener.config.RabbitMQConfig;
import com.example.linkshortener.data.dto.ClickEvent;
import com.example.linkshortener.data.dto.CreationRequest;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.service.CacheService;
import com.example.linkshortener.service.DataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public final class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private DataService dataService;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${features.analytics.rabbitmq.enabled}")
    private boolean rabbitMqAnalyticsEnabled;

    @PostMapping
    public ResponseEntity<String> createShortUrl(
           @Valid @RequestBody CreationRequest request
    ) {
        try {
            String shortenedUrl = dataService.shortenUrl(request);
            return ResponseEntity.ok(shortenedUrl);
        } catch (SQLIntegrityConstraintViolationException e) {
            return ResponseEntity.status(409).body("Custom shortened URL already exists.");
        }
    }

    @GetMapping("/{shortenedUrl}")
    public ResponseEntity<String> getOriginalUrl(@PathVariable String shortenedUrl) {
        String originalUrl = dataService.findOrigin(shortenedUrl);
        if (originalUrl != null) {
            if (rabbitMqAnalyticsEnabled) {
                // RabbitMQ Asynchronous Path
                try {
                    log.debug("Publishing click event for {}", shortenedUrl);
                    ClickEvent event = new ClickEvent(shortenedUrl, LocalDateTime.now());
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.EXCHANGE_NAME,
                            RabbitMQConfig.CLICK_ROUTING_KEY,
                            event
                    );
                } catch (Exception e) {
                    // Log the error, but DO NOT fail the redirect.
                    log.error("Failed to send click event to RabbitMQ for {}: {}", shortenedUrl, e.getMessage());
                }
            } else {
                // Fallback path using CacheService
                log.debug("Using fallback (CacheService) click tracking for {}", shortenedUrl);
                cacheService.incrementClickCount(shortenedUrl); // Track clickCount
            }

            return ResponseEntity.ok(originalUrl);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Data>> getAllUrls(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(dataService.findAll(page, size));
    }
    @DeleteMapping("/{shortenedUrl}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortenedUrl) {
        dataService.deleteUrl(shortenedUrl);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllUrls() {
        dataService.deleteAll();
        return ResponseEntity.noContent().build();
    }
} 