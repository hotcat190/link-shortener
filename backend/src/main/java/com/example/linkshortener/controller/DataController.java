package com.example.linkshortener.controller;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.service.DataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@CrossOrigin(origins = "*") // Vite's default port
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public final class DataController {
    @Autowired
    private DataService dataService;


    @PostMapping("/create")
    public ResponseEntity<String> createShortUrl(
            @RequestParam String url,
            @RequestParam(required = false) Long ttlMinute,
            @RequestParam(required = false) String customShortenedUrl
    ) {
        try {
            String shortenedUrl = dataService.shortenUrl(url, ttlMinute, customShortenedUrl);
            return ResponseEntity.ok(shortenedUrl);
        } catch (SQLIntegrityConstraintViolationException e) {
            return ResponseEntity.status(409).body("Custom shortened URL already exists.");
        }
    }

    @GetMapping("/short/{shortenedUrl}")
    public ResponseEntity<String> getOriginalUrl(@PathVariable String shortenedUrl) {
        String originalUrl = dataService.findOrigin(shortenedUrl);
        if (originalUrl != null) {
            return ResponseEntity.ok(originalUrl);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Data>> getAllUrls(HttpServletRequest request) {
        return ResponseEntity.ok(dataService.findAll());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteShortUrl(@RequestParam String shortenedUrl) {
        dataService.deleteUrl(shortenedUrl);
        return ResponseEntity.noContent().build();
    }
} 