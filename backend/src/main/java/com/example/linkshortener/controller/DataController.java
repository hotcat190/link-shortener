package com.example.linkshortener.controller;

import com.example.linkshortener.data.dto.CreationRequest;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.service.CacheService;
import com.example.linkshortener.service.DataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public final class DataController {
    @Autowired
    private DataService dataService;
    @Autowired
    private CacheService cacheService;

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
            cacheService.incrementClickCount(shortenedUrl); // Track clickCount 
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