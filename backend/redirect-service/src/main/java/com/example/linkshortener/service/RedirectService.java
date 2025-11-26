package com.example.linkshortener.service;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RedirectService {

    private final DataRepository dataRepository;
    private final RedirectCacheService cacheService;

    public String findOriginalUrl(String shortenedUrl) {
        // 1. Try Cache
        String cachedUrl = cacheService.getOriginalUrl(shortenedUrl);
        if (cachedUrl != null) {
            cacheService.incrementClickCount(shortenedUrl);
            return cachedUrl;
        }

        // 2. Try Database (Cache Miss)
        Data data = dataRepository.findByShortenedUrl(shortenedUrl).orElse(null);

        if (data != null) {
            // Check Expiration
            if (data.getExpirationTime() != null && data.getExpirationTime().isBefore(LocalDateTime.now())) {
                dataRepository.delete(data);
                return null;
            }

            // 3. Populate Cache (Read Repair)
            long ttl = (data.getExpirationTime() != null) 
                ? Duration.between(LocalDateTime.now(), data.getExpirationTime()).toSeconds() 
                : 0; // 0 usually implies no TTL logic here, handled in service
            
            // If no expiration, pass null to persist indefinitely
            cacheService.saveToCache(shortenedUrl, data.getUrl(), ttl > 0 ? ttl : null);
            
            // 4. Track Click
            cacheService.incrementClickCount(shortenedUrl);
            
            return data.getUrl();
        }

        return null;
    }
}