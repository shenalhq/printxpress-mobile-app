package com.assignment.printxpress.util;

import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public final class PasswordUtils {
    private static final int ITERATIONS = 12000;
    private static final int KEY_LENGTH = 256;

    private PasswordUtils() {}

    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            byte[] encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.encodeToString(encoded, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to secure password", e);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        return hash(password, salt).equals(expectedHash);
    }
}
