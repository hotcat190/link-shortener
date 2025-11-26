package com.example.linkshortener.service;

import com.example.linkshortener.data.repository.DataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedirectCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedirectCacheService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final DataRepository dataRepository;

    // 1. Get from Cache
    public String getOriginalUrl(String shortenedUrl) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String key = "url:" + shortenedUrl;
        if (hashOps.hasKey(key, "url")) {
            return hashOps.get(key, "url");
        }
        return null;
    }

    // 2. Save to Cache (Read-through)
    public void saveToCache(String shortenedUrl, String originalUrl, Long ttlSeconds) {
        String key = "url:" + shortenedUrl;
        redisTemplate.opsForHash().put(key, "url", originalUrl);
        if (ttlSeconds != null) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.persist(key);
        }
    }

    // 3. Increment Clicks (Fast Write)
    public void incrementClickCount(String shortenedUrl) {
        String key = "clicks:" + shortenedUrl;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 2, TimeUnit.HOURS); // Keep alive long enough to sync
    }

    // 4. Sync Task (Write-Behind)
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void syncClickCountsToDatabase() {
        Set<String> keys = redisTemplate.keys("clicks:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                String shortenedUrl = key.replace("clicks:", "");
                String value = redisTemplate.opsForValue().get(key);
                
                if (value == null) continue;

                long countToAdd = Long.parseLong(value);
                if (countToAdd == 0) continue;

                logger.info("Syncing {} clicks for {}", countToAdd, shortenedUrl);

                // Update DB safely
                dataRepository.findByShortenedUrl(shortenedUrl).ifPresent(data -> {
                    data.setClickCount(data.getClickCount() + (int) countToAdd);
                    dataRepository.save(data);
                });

                // Decrement the value we just processed or delete if we want simple statelessness
                // NOTE: A safer approach in distributed systems is "get and delete", 
                // but for this scale, deleting the key after sync is acceptable.
                redisTemplate.delete(key);
                
            } catch (Exception e) {
                logger.error("Failed to sync clicks for key: " + key, e);
            }
        }
    }
}