package com.example.ecommerce_backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    
    @Value("${application.rate-limit.capacity:100}")
    private long capacity;
    
    @Value("${application.rate-limit.refill-rate:10}")
    private long refillRate;
    
    @Value("${application.rate-limit.window-duration:60}")
    private long windowDuration;
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }
    
    private Bucket createNewBucket() {
        // New Bucket4j 8.x API
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillRate, Duration.ofSeconds(windowDuration))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}