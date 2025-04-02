package com.example.linkshortener.controller;

import com.example.linkshortener.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {
    private final DataService dataService;

    @PostMapping("/shorten")
    public ResponseEntity<String> createShortUrl(@RequestBody String url) {
        return ResponseEntity.ok(dataService.shortUrl(url));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getOriginalUrl(@PathVariable String id) {
        return ResponseEntity.ok(dataService.findOrigin(id));
    }
} 