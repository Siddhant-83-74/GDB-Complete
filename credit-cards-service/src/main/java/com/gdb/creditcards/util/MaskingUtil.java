package com.gdb.creditcards.util;

/**
 * b.12 Mask all digits of a PAN except the last four.
 */
public final class MaskingUtil {

    private MaskingUtil() {
    }

    /**
     * Masks from the full PAN: "4111111111111234" -> "**** **** **** 1234".
     */
    public static String mask(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        String last4 = pan.substring(pan.length() - 4);
        return "**** **** **** " + last4;
    }

    /**
     * Masks given only the last four digits (already extracted/stored).
     */
    public static String maskFromLast4(String last4) {
        String safe = (last4 == null) ? "****" : last4;
        return "**** **** **** " + safe;
    }
}
