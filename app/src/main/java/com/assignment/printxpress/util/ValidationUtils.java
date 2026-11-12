package com.assignment.printxpress.util;

import android.util.Patterns;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public final class ValidationUtils {
    private ValidationUtils() {}
    public static boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
    public static boolean validEmail(String value) { return hasText(value) && Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches(); }
    public static boolean validPhone(String value) { return hasText(value) && value.replaceAll("\\s+", "").matches("^(\\+94|0)7[0-9]{8}$"); }
    public static boolean strongEnoughPassword(String value) { return value != null && value.length() >= 6; }
}
