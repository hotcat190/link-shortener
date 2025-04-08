package com.example.linkshortener.service;

import com.example.linkshortener.config.RateLimiterConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public final class RateLimitingService {
    private final RateLimiterConfig rateLimiterConfig;

    private final Map<String, Bucket> buckets;

    @Autowired
    public RateLimitingService(RateLimiterConfig rateLimiterConfig, Map<String, Bucket> buckets) {
        this.rateLimiterConfig = rateLimiterConfig;
        this.buckets = buckets;
    }

    @Value("${rate.limit.enabled:false}") // false is default if not set
    private boolean enabled;

    public boolean ok(HttpServletRequest request) {
        if (enabled) {
            String clientId = getClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(clientId, k -> rateLimiterConfig.createNewBucket());
            return bucket.tryConsume(1);
        } else {
            return true;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
