
package com.fooddelivery.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms:3600000}")
            long expirationMillis) {

        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    // Generate a JWT containing the customer's email and role
    public String generateToken(String email, String role) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(now.plusMillis(expirationMillis))
                )
                .signWith(signingKey)
                .compact();
    }

    // Compatibility method for existing calls
    public String generateToken(String email) {
        return generateToken(email, "CUSTOMER");
    }

    // Extract email from a valid token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Extract role from a valid token
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // Validate token signature, email and expiry
    public boolean isTokenValid(String token, String expectedEmail) {

        try {
            Claims claims = extractClaims(token);

            return expectedEmail != null
                    && expectedEmail.equalsIgnoreCase(
                            claims.getSubject())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
