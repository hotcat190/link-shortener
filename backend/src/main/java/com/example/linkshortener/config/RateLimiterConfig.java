package com.example.linkshortener.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.SynchronizationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Map<String, Bucket> buckets() {
        return new ConcurrentHashMap<>();
    }

    // Create a bucket with a sliding window of 10 requests per minute
    public Bucket createNewBucket() {
        // Using interval refill with small intervals creates a sliding window effect
        // This refills tokens gradually (1 token every 6 seconds) instead of all at once
        Refill refill = Refill.intervally(1, Duration.ofSeconds(6));
        Bandwidth limit = Bandwidth.classic(10, refill);

        return Bucket.builder()
                .addLimit(limit)
                // Use thread-safe synchronization for concurrent access
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build();
    }
}

