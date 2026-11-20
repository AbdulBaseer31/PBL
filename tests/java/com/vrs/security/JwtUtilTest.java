package com.vrs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private final String validSecret = "this_is_a_secret_key_with_at_least_32_characters_12345";
    private final JwtUtil jwtUtil = new JwtUtil(validSecret, 60);

    @Test
    void secretUnder32Chars_throwsException() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("short_secret", 60));
    }

    @Test
    void tokenRoundTrip_returnsSameSubjectAndRole() {
        String token = jwtUtil.createToken("C123456", "CUSTOMER");
        Claims claims = jwtUtil.readToken(token);
        assertEquals("C123456", claims.getSubject());
        assertEquals("CUSTOMER", claims.get("role"));
    }

    @Test
    void expiredToken_throwsExpiredJwtException() {
        JwtUtil expiredUtil = new JwtUtil(validSecret, -5);
        String token = expiredUtil.createToken("C123456", "CUSTOMER");
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.readToken(token));
    }

    @Test
    void tamperedToken_throwsJwtException() {
        String token = jwtUtil.createToken("C123456", "CUSTOMER");
        String tampered = token.substring(0, token.length() - 5) + "abcde";
        assertThrows(JwtException.class, () -> jwtUtil.readToken(tampered));
    }

    @Test
    void differentSecret_throwsJwtException() {
        JwtUtil anotherUtil = new JwtUtil("another_valid_secret_key_at_least_32_characters_67890", 60);
        String token = anotherUtil.createToken("C123456", "CUSTOMER");
        assertThrows(JwtException.class, () -> jwtUtil.readToken(token));
    }
}

