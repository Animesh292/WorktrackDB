package com.myapp.worktrack_backend.registration.security;

import java.security.SecureRandom;

public class TemporaryPasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789" +
            "!@#$%^&*";

    public static String generate(int length) {
        StringBuilder password = new StringBuilder(length);

        for(int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}
