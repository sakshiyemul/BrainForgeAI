package com.sakshi.brainforgeai.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testGenerateAndValidateToken() {
        String email = "alice@example.com";
        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
    }
}
