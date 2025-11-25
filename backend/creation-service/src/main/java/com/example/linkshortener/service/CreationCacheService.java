package com.example.linkshortener.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CreationCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final HashOperations<String, String, String> hashOps;

    @Autowired
    public CreationCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    public void saveToCache(String shortenedUrl, String originalUrl, Long ttlSecond) {
        String key = "url:" + shortenedUrl;
        hashOps.put(key, "url", originalUrl);
        
        if (ttlSecond != null) {
            redisTemplate.expire(key, ttlSecond, TimeUnit.SECONDS);
        } else {
            redisTemplate.persist(key);
        }
    }
}