package com.ansh.smart_commerce.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void generateToken_shouldEmbedEmailAndValidate() {
        JwtService jwtService = new JwtService(SECRET, 60_000L);

        String token = jwtService.generateToken("user@example.com");

        assertEquals("user@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_shouldFailForExpiredToken() {
        JwtService jwtService = new JwtService(SECRET, -1L);

        String token = jwtService.generateToken("expired@example.com");

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(token));
    }
}