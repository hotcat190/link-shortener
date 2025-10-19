package com.example.linkshortener.controller;

import com.example.linkshortener.data.dto.CreationRequest;
import com.example.linkshortener.data.dto.CreationResponse;
import com.example.linkshortener.data.message.ClickTrackingMessage;
import com.example.linkshortener.data.message.UrlShorteningMessage;
import com.example.linkshortener.service.MessageProducerService;
import com.example.linkshortener.service.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class UrlController {
    private final MessageProducerService messageProducer;
    private final CacheService cacheService;

    public UrlController(MessageProducerService messageProducer, CacheService cacheService) {
        this.messageProducer = messageProducer;
        this.cacheService = cacheService;
    }

    @PostMapping
    public ResponseEntity<?> createShortUrl(@RequestBody CreationRequest request, HttpServletRequest httpRequest) {
        UrlShorteningMessage message = new UrlShorteningMessage(
            request.getUrl(),
            request.getCustomShortenedUrl(),
            request.getTtlMinute()
        );

        messageProducer.sendUrlShorteningMessage(message);

        // Return a response with status
        return ResponseEntity.accepted().body(new CreationResponse("URL shortening request accepted"));
    }

    @GetMapping("/{shortenedUrl}")
    public ResponseEntity<?> redirectToOriginalUrl(
            @PathVariable String shortenedUrl,
            HttpServletRequest request) {
        
        String originalUrl = cacheService.getOriginalUrlFromCache(shortenedUrl);
        
        if (originalUrl != null) {
            // Track the click asynchronously
            messageProducer.sendClickTrackingMessage(new ClickTrackingMessage(
                shortenedUrl,
                request.getHeader("X-Forwarded-For"),
                Instant.now().toEpochMilli()
            ));

            return ResponseEntity.ok().body(originalUrl);
        }

        return ResponseEntity.notFound().build();
    }
}