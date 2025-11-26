package com.example.linkshortener.controller;

import lombok.RequiredArgsConstructor;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.linkshortener.data.dto.CreationRequest;
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
}