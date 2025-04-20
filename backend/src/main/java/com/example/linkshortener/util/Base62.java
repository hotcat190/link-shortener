package com.example.linkshortener.util;

public class Base62 {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    /**
     * Encode a number to a Base62 string.
     * @param num the number to encode
     * @return the Base62 encoded string
     */
    public static String encode(long num) {
        if (num < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }

        if (num == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(ALPHABET.charAt((int)(num % BASE)));
            num /= BASE;
        }
        return sb.reverse().toString();
    }
}
