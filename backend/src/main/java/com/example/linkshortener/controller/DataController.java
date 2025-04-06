package com.example.linkshortener.controller;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class DataController {
    private final DataService dataService;

    @PostMapping("/create")
    public ResponseEntity<String> createShortUrl(
            @RequestParam String url,
            @RequestParam(required = false) Long ttlMinute,
            @RequestParam(required = false) String customShortenedUrl
    ) {
        return ResponseEntity.ok(dataService.shortenUrl(url, ttlMinute, customShortenedUrl));
    }

    @GetMapping("/short/{shortenedUrl}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortenedUrl) {
        String originalUrl = dataService.findOrigin(shortenedUrl);
        if (originalUrl != null) {
            return ResponseEntity.status(302) // HTTP 302 Found
                    .location(URI.create(originalUrl))
                    .build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Data>> getAllUrls() {
        return ResponseEntity.ok(dataService.findAll());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteShortUrl(@RequestParam String shortenedUrl) {
        dataService.deleteUrl(shortenedUrl);
        return ResponseEntity.noContent().build();
    }
} 