package com.assignment.printxpress.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public final class FormatUtils {
    private FormatUtils() {}
    public static String money(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("en", "LK"));
        format.setMaximumFractionDigits(0);
        return "LKR " + format.format(value);
    }
}
