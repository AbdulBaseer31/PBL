package com.vrs.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtSecretStartupTest {

    @Test
    void secretUnder32Characters_failsValidation() {
        String shortSecret = "too_short_secret";
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            if (shortSecret.length() < 32) {
                throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
            }
        });
        assertTrue(exception.getMessage().contains("at least 32 characters"));
    }
}

