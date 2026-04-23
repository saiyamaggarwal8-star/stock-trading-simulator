package com.trading.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Hashes a plaintext password using BCrypt.
     */
    public static String hashPassword(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(12));
    }

    /**
     * Checks if a plaintext password matches a hashed password.
     */
    public static boolean checkPassword(String plaintext, String hashed) {
        if (plaintext == null || hashed == null) {
            return false;
        }
        return BCrypt.checkpw(plaintext, hashed);
    }
}
