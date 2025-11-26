package com.example.linkshortener.controller;

import lombok.RequiredArgsConstructor;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.linkshortener.data.dto.CreationRequest;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.service.CreationService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public final class CreationController {

    @Autowired
    private CreationService creationService;

    @PostMapping
    public ResponseEntity<String> createShortUrl(
           @Valid @RequestBody CreationRequest request
    ) {
        try {
            String shortenedUrl = creationService.shortenUrl(request);
            return ResponseEntity.ok(shortenedUrl);
        } catch (SQLIntegrityConstraintViolationException e) {
            return ResponseEntity.status(409).body("Custom shortened URL already exists.");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Data>> getAllUrls(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(creationService.findAll(page, size));
    }
    @DeleteMapping("/{shortenedUrl}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortenedUrl) {
        creationService.deleteUrl(shortenedUrl);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllUrls() {
        creationService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}