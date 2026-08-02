package com.visibleai.brasstacks.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "brasstacks-test-secret-must-be-at-least-256-bits-long-for-hmac-sha256!",
                3600000L,   // 1 hour access token
                2592000000L // 30 day refresh token
        );
    }

    @Test
    void generateAccessToken_producesValidToken() {
        String token = jwtUtil.generateAccessToken("test@example.com", 1L, null);

        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertTrue(jwtUtil.isAccessToken(token));
        assertFalse(jwtUtil.isRefreshToken(token));
    }

    @Test
    void generateRefreshToken_producesValidToken() {
        String token = jwtUtil.generateRefreshToken("test@example.com", 1L, null);

        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertTrue(jwtUtil.isRefreshToken(token));
        assertFalse(jwtUtil.isAccessToken(token));
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateAccessToken("test@example.com", 1L, null);

        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = jwtUtil.generateAccessToken("test@example.com", 42L, null);

        assertEquals(42L, jwtUtil.extractUserId(token));
    }

    @Test
    void isValid_returnsFalseForExpiredToken() {
        JwtUtil shortLivedJwt = new JwtUtil(
                "brasstacks-test-secret-must-be-at-least-256-bits-long-for-hmac-sha256!",
                -1000L, // already expired
                -1000L
        );

        String token = shortLivedJwt.generateAccessToken("test@example.com", 1L, null);
        assertFalse(shortLivedJwt.isValid(token));
    }

    @Test
    void isValid_returnsFalseForGarbageToken() {
        assertFalse(jwtUtil.isValid("not.a.valid.token"));
    }

    @Test
    void isValid_returnsFalseForEmptyToken() {
        assertFalse(jwtUtil.isValid(""));
    }

    @Test
    void accessAndRefreshTokens_haveDifferentTypes() {
        String access = jwtUtil.generateAccessToken("test@example.com", 1L, null);
        String refresh = jwtUtil.generateRefreshToken("test@example.com", 1L, null);

        assertEquals("access", jwtUtil.extractType(access));
        assertEquals("refresh", jwtUtil.extractType(refresh));
    }

    @Test
    void differentUsers_getDifferentTokens() {
        String token1 = jwtUtil.generateAccessToken("user1@example.com", 1L, null);
        String token2 = jwtUtil.generateAccessToken("user2@example.com", 2L, null);

        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", jwtUtil.extractEmail(token1));
        assertEquals("user2@example.com", jwtUtil.extractEmail(token2));
    }
}
