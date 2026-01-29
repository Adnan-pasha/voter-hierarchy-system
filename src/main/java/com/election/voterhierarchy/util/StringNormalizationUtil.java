package com.election.voterhierarchy.util;

public class StringNormalizationUtil {

    /**
     * Normalizes a string by:
     * 1. Trimming leading and trailing spaces
     * 2. Collapsing multiple spaces into a single space
     * 
     * Example: "  Ghouse   Mohiddin " → "Ghouse Mohiddin"
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim and collapse multiple spaces
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normalizes all string fields in an object (used for batch processing)
     */
    public static String normalizeIfNotNull(String input) {
        return input == null ? null : normalize(input);
    }

    /**
     * Case-insensitive comparison after normalization
     */
    public static boolean equalsIgnoreCaseNormalized(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return normalize(str1).equalsIgnoreCase(normalize(str2));
    }
}
