package com.example.linkshortener.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final HashOperations<String, String, String> hashOps;

    @Autowired
    public CacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // Save URL to cache
    public void saveToCache(String shortenedUrl, String originalUrl, String ttl) {
        String key = "url:" + shortenedUrl;

        // Save the URL and other attributes to the Redis hash
        hashOps.put(key, "url", originalUrl);
        // Optionally, store other data (creation time, expiration) in separate fields
        hashOps.put(key, "ttl", ttl);

        // Set expiration for the key (optional)
        redisTemplate.expire(key, Long.parseLong(ttl), TimeUnit.MINUTES);
    }

    // Retrieve original URL from cache
    public String getOriginalUrlFromCache(String shortenedUrl) {
        String key = "url:" + shortenedUrl;

        // Check if the URL exists in the cache
        if (hashOps.hasKey(key, "url")) {
            return hashOps.get(key, "url");
        }
        return null; // Or handle it accordingly
    }

    // Delete URL from cache
    public void deleteFromCache(String shortenedUrl) {
        String key = "url:" + shortenedUrl;

        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key); // Delete the cache entry
        }
    }
}