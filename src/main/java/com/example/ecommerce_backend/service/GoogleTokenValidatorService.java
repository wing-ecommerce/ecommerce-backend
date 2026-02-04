package com.example.ecommerce_backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class GoogleTokenValidatorService {
    
    @Value("${oauth.google.client-id}")
    private String googleClientId;
    
    private GoogleIdTokenVerifier verifier;
    
    /**
     * Validates a Google ID token and returns the payload.
     * 
     * @param idToken The Google ID token to validate
     * @return GoogleIdToken.Payload if valid, null otherwise
     */
    public GoogleIdToken.Payload validateToken(String idToken) {
        try {
            // Initialize verifier if needed
            if (verifier == null) {
                verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    new GsonFactory()
                )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
            }
            
            // Verify the token
            GoogleIdToken token = verifier.verify(idToken);
            
            if (token != null) {
                GoogleIdToken.Payload payload = token.getPayload();
                
                // Log verification success
                log.info("Google token validated for user: {}", payload.getEmail());
                
                // Check email verification
                Boolean emailVerified = payload.getEmailVerified();
                if (emailVerified == null || !emailVerified) {
                    log.warn("Email not verified for Google user: {}", payload.getEmail());
                }
                
                return payload;
            } else {
                log.error("Invalid Google ID token - verification failed");
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error validating Google token: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public String getEmail(GoogleIdToken.Payload payload) {
        return payload.getEmail();
    }
    
    public String getProviderId(GoogleIdToken.Payload payload) {
        return payload.getSubject();
    }
    
    public Boolean isEmailVerified(GoogleIdToken.Payload payload) {
        return payload.getEmailVerified();
    }
}