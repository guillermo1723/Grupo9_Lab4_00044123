package com.server.app.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import com.server.app.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JsonWebToken {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JsonWebToken(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long expirationTime
    ) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret-key debe tener al menos 32 caracteres");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = expirationTime;
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("id", user.getId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer extractUserId(String token) {
        Object id = parseClaims(token).get("id");
        if (id instanceof Integer integer) {
            return integer;
        }

        if (id instanceof Long longId) {
            return longId.intValue();
        }

        if (id instanceof String stringId) {
            return Integer.parseInt(stringId);
        }

        throw new BadCredentialsException("Token inválido");
    }
}