package com.example.ecommerce_backend.interceptor;

import com.example.ecommerce_backend.config.RateLimitConfig;
import com.example.ecommerce_backend.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimitConfig rateLimitConfig;
    
    @Value("${application.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        
        if (!rateLimitEnabled) {
            return true;
        }
        
        // Get IP address
        String ip = getClientIP(request);
        
        // Resolve bucket for this IP
        Bucket bucket = rateLimitConfig.resolveBucket(ip);
        
        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Try again in " + waitForRefill + " seconds."
            );
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}