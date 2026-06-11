package com.DB.DBmarket.pojo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    private static final String PREFIX = "sha256$";
    private static final String SALT = "DBTakeOut";

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return PREFIX + sha256(SALT + ":" + rawPassword);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (!storedPassword.startsWith(PREFIX)) {
            return rawPassword.equals(storedPassword);
        }
        return hash(rawPassword).equals(storedPassword);
    }

    public static boolean isHashed(String password) {
        return password != null && password.startsWith(PREFIX);
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
