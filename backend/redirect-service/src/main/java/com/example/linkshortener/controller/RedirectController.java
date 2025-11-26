package com.example.linkshortener.controller;

import com.example.linkshortener.service.RedirectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final RedirectService redirectService;

    @GetMapping("/{shortenedUrl}")
    public ResponseEntity<String> redirect(@PathVariable String shortenedUrl) {
        String originalUrl = redirectService.findOriginalUrl(shortenedUrl);
        
        if (originalUrl != null) {
            // Note: In a real redirector, you might return HttpStatus.FOUND (302) 
            // and a "Location" header. For now, we return the string as requested.
            return ResponseEntity.ok(originalUrl);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}