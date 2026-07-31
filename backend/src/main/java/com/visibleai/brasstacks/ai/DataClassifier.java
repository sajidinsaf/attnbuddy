package com.visibleai.brasstacks.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Strips personal data from text before sending to AI providers.
 * Conservative approach: redact anything that looks personal.
 */
@Component
public class DataClassifier {

    // Email addresses
    private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    // Phone numbers (various formats)
    private static final Pattern PHONE = Pattern.compile("\\+?\\d{1,4}[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}");
    // URLs
    private static final Pattern URL = Pattern.compile("https?://\\S+");
    // Dates with specific formats that might identify someone
    private static final Pattern SPECIFIC_DATE = Pattern.compile("\\b\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}\\b");
    // Money amounts
    private static final Pattern MONEY = Pattern.compile("\\$\\d+[,\\d]*\\.?\\d*");

    /**
     * Anonymize text by removing personal identifiers.
     * Returns a cleaned version safe to send to AI providers.
     */
    public String anonymize(String text) {
        if (text == null) return "";

        String result = text;
        result = EMAIL.matcher(result).replaceAll("[email]");
        result = PHONE.matcher(result).replaceAll("[phone]");
        result = URL.matcher(result).replaceAll("[url]");
        result = SPECIFIC_DATE.matcher(result).replaceAll("[date]");
        result = MONEY.matcher(result).replaceAll("[amount]");

        return result.trim();
    }

    /**
     * Check if text contains potentially sensitive data.
     */
    public boolean containsSensitiveData(String text) {
        if (text == null) return false;
        return EMAIL.matcher(text).find()
                || PHONE.matcher(text).find()
                || URL.matcher(text).find();
    }
}
