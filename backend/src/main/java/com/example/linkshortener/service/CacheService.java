package com.example.linkshortener.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import com.example.linkshortener.data.repository.DataRepository;

@Service
public class CacheService {

    Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private DataRepository dataRepository;
    private final HashOperations<String, String, String> hashOps;

    @Autowired
    public CacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // Save URL to cache
    public void saveToCache(String shortenedUrl, String originalUrl, Long ttlSecond) {
        String key = "url:" + shortenedUrl;

        // Save the URL and other attributes to the Redis hash
        hashOps.put(key, "url", originalUrl);
        // Optionally, store other data (creation time, expiration) in separate fields
        // Set expiration for the key (optional)
        if (ttlSecond != null) {
            redisTemplate.expire(key, ttlSecond, TimeUnit.SECONDS);
        } else {
            // If there's no ttlSecond, remove any expiration on the key, making it persist indefinitely
            redisTemplate.persist(key);  // This removes the TTL (sets it to never expire)
        }
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
        redisTemplate.delete("url:" + shortenedUrl);
    }

    public void incrementClickCount(String shortenedUrl) {
        String key = "clicks:" + shortenedUrl;
        redisTemplate.opsForValue().increment(key);
        //logger.debug("Click count for " + shortenedUrl + " is: " + redisTemplate.opsForValue().get(key));
        redisTemplate.expire(key, 1, TimeUnit.HOURS); // Optional
    }

    @Scheduled(fixedRate = 60 * 1000) // Every 1 minutes
    public void syncClickCountsToDatabase() {
        Set<String> keys = redisTemplate.keys("clicks:*");

        if (keys != null) {
            for (String key : keys) {
                String shortenedUrl = key.replace("clicks:", "");
                String value = redisTemplate.opsForValue().get(key);

                try {
                    long clickIncrement = Long.parseLong(value) / 2;
                    logger.debug("Click count for " + shortenedUrl + " is: " + clickIncrement);

                    dataRepository.findByShortenedUrl(shortenedUrl).ifPresent(data -> {
                        data.setClickCount(data.getClickCount() + (int) clickIncrement);
                        dataRepository.save(data);
                    });
                } catch (NumberFormatException e) {
                    // If the value is not a number, log the error but don't crash.
                    logger.error("Could not parse click count for key '" + key + "'. Value was: '" + value + "'");
                } finally {
                    // IMPORTANT: Always delete the key after attempting to process it,
                    // whether it succeeded or failed, to prevent it from being processed again.
                    redisTemplate.delete(key);
                }
            }
        }
    }
}