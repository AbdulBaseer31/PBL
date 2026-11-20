package com.vrs.util;

import java.security.SecureRandom;

public class IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String next(String prefix) {
        int number = 100000 + RANDOM.nextInt(900000);
        return prefix + number;
    }
}

