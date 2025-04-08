package com.example.linkshortener.controller;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.service.DataService;
import com.example.linkshortener.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public final class DataController {
    @Autowired
    private DataService dataService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @PostMapping("/create")
    public ResponseEntity<String> createShortUrl(
            @RequestParam String url,
            @RequestParam(required = false) Long ttlMinute,
            @RequestParam(required = false) String customShortenedUrl,
            HttpServletRequest request
    ) {
        if (rateLimitingService.ok(request)) {
            return ResponseEntity.ok(dataService.shortenUrl(url, ttlMinute, customShortenedUrl));
        } else {
            return ResponseEntity.status(429).build(); // HTTP 429 Too Many Requests
        }
    }

    @GetMapping("/short/{shortenedUrl}")
    public ResponseEntity<String> getOriginalUrl(@PathVariable String shortenedUrl, HttpServletRequest request) {
        if (rateLimitingService.ok(request)) {
            String originalUrl = dataService.findOrigin(shortenedUrl);
            if (originalUrl != null) {
                return ResponseEntity.ok(originalUrl);

            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(429).build(); // HTTP 429 Too Many Requests
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Data>> getAllUrls(HttpServletRequest request) {
        if (rateLimitingService.ok(request)) {
            return ResponseEntity.ok(dataService.findAll());
        } else {
            return ResponseEntity.status(429).build(); // HTTP 429 Too Many Requests
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteShortUrl(@RequestParam String shortenedUrl, HttpServletRequest request) {
        if (rateLimitingService.ok(request)) {
            dataService.deleteUrl(shortenedUrl);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(429).build();
        }
    }
} 