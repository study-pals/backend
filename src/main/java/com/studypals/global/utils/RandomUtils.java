package com.studypals.global.utils;

import java.security.SecureRandom;

public class RandomUtils {
    private static final String CHARACTERS_ALPHA_NUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHARACTERS_UPPERCASE_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateAlphaNumericCode(int length) {
        return generateRandomCode(length, CHARACTERS_ALPHA_NUMERIC);
    }

    public static String generateUpperAlphaNumericCode(int length) {
        return generateRandomCode(length, CHARACTERS_UPPERCASE_NUMERIC);
    }

    private static String generateRandomCode(int length, String characterPool) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(characterPool.length());
            builder.append(characterPool.charAt(idx));
        }

        return builder.toString();
    }
}
