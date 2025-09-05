package com.meetThePeople.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    
    @Value("${app.jwt.secret}")
    private String secret;
    
    @Value("${app.jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        try {
            // Try to use the configured secret if it's long enough
            byte[] keyBytes = secret.getBytes();
            if (keyBytes.length >= 64) { // 64 bytes = 512 bits
                log.info("Using configured secret key ({} bytes)", keyBytes.length);
                return Keys.hmacShaKeyFor(keyBytes);
            } else {
                log.info("Configured secret key is too short ({} bytes < 64 bytes), generating secure key", keyBytes.length);
            }
        } catch (Exception e) {
            log.info("Error using configured secret key, generating secure key: {}", e.getMessage());
        }
        
        // Generate a secure key guaranteed to work with HS512
        log.info("Generating secure key for HS512 algorithm");
        return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }
    
    public String generateToken(String mobile) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);
            
            log.info("Generating JWT token for mobile: {}", mobile);
            String token = Jwts.builder()
                    .setSubject(mobile)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
            
            log.info("JWT token generated successfully for mobile: {}", mobile);
            return token;
        } catch (Exception e) {
            log.error("Error generating JWT token for mobile: {}", mobile, e);
            throw new RuntimeException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }
    
    public String getMobileFromToken(String token) {
        try {
            log.info("Extracting mobile from JWT token");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String mobile = claims.getSubject();
            log.info("Successfully extracted mobile: {} from JWT token", mobile);
            return mobile;
        } catch (JwtException e) {
            log.info("JWT token parsing failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error extracting mobile from JWT token", e);
            throw new RuntimeException("Failed to extract mobile from JWT token: " + e.getMessage(), e);
        }
    }
    
    public boolean validateToken(String token) {
        try {
            log.info("Validating JWT token");
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            
            log.info("JWT token validation successful");
            return true;
        } catch (JwtException e) {
            log.info("JWT token validation failed - JWT exception: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.info("JWT token validation failed - Invalid argument: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT token validation failed - Unexpected error", e);
            return false;
        }
    }
    
    /**
     * Get information about the current signing key
     */
    public String getKeyInfo() {
        try {
            SecretKey key = getSigningKey();
            String algorithm = key.getAlgorithm();
            int keyLength = key.getEncoded().length * 8; // Convert bytes to bits
            
            return String.format("Algorithm: %s, Key Length: %d bits, Key Type: %s", 
                    algorithm, keyLength, key.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Error getting key information", e);
            return "Error getting key information: " + e.getMessage();
        }
    }
    
    /**
     * Get the JWT expiration time in milliseconds
     */
    public Long getExpiration() {
        return expiration;
    }
} 