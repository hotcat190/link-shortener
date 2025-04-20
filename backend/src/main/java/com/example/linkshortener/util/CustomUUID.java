package com.example.linkshortener.util;

import java.util.Random;

import static com.aventrix.jnanoid.jnanoid.NanoIdUtils.randomNanoId;

public class CustomUUID {
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 16;
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static String random(Random random) {
        int length = random.nextInt(MAX_LENGTH - MIN_LENGTH + 1) + MIN_LENGTH;
        return randomNanoId(random, ALPHABET, length);
    }
}
