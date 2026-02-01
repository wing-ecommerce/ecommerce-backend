package com.example.ecommerce_backend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for managing secure HTTP-only cookies
 */
@Component
public class CookieUtil {
    
    @Value("${application.security.cookie.name:refreshToken}")
    private String cookieName;
    
    @Value("${application.security.cookie.max-age:604800}")
    private int maxAge; // 7 days in seconds
    
    @Value("${application.security.cookie.http-only:true}")
    private boolean httpOnly;
    
    @Value("${application.security.cookie.secure:false}")
    private boolean secure; // Should be true in production (HTTPS)
    
    @Value("${application.security.cookie.same-site:Strict}")
    private String sameSite; // Strict, Lax, or None
    
    @Value("${application.security.cookie.path:/api/v1/auth}")
    private String path;
    
    /**
     * Create a secure refresh token cookie
     */
    public void createRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        
        // SameSite attribute (requires manual setting)
        String cookieValue = String.format("%s=%s; Path=%s; Max-Age=%d; %s; %s; SameSite=%s",
                cookieName,
                token,
                path,
                maxAge,
                httpOnly ? "HttpOnly" : "",
                secure ? "Secure" : "",
                sameSite
        );
        
        response.addHeader("Set-Cookie", cookieValue);
    }
    
    /**
     * Get refresh token from cookie
     */
    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
    
    /**
     * Delete refresh token cookie (logout)
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(0); // Delete immediately
        
        String cookieValue = String.format("%s=; Path=%s; Max-Age=0; %s; %s; SameSite=%s",
                cookieName,
                path,
                httpOnly ? "HttpOnly" : "",
                secure ? "Secure" : "",
                sameSite
        );
        
        response.addHeader("Set-Cookie", cookieValue);
    }
}