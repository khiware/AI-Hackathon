package com.askbit.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PiiRedactionService {

    @Value("${askbit.ai.pii-redaction.enabled:true}")
    private boolean piiRedactionEnabled;

    private static final Map<String, Pattern> PII_PATTERNS = new HashMap<>();

    static {
        // Email pattern
        PII_PATTERNS.put("EMAIL", Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
        ));

        // Phone number patterns (US and International)
        PII_PATTERNS.put("PHONE", Pattern.compile(
            "\\b(?:\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b|\\b\\+?[0-9]{1,3}[-.]?[0-9]{3,14}\\b"
        ));

        // Social Security Number (US)
        PII_PATTERNS.put("SSN", Pattern.compile(
            "\\b(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}\\b"
        ));

        // Credit Card Numbers
        PII_PATTERNS.put("CREDIT_CARD", Pattern.compile(
            "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|6(?:011|5[0-9]{2})[0-9]{12}|(?:2131|1800|35\\d{3})\\d{11})\\b"
        ));

        // IP Addresses
        PII_PATTERNS.put("IP_ADDRESS", Pattern.compile(
            "\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b"
        ));

        // US Address Pattern (simplified)
        PII_PATTERNS.put("ADDRESS", Pattern.compile(
            "\\b\\d{1,5}\\s+([A-Z][a-z]+\\s+)+(Street|St|Avenue|Ave|Road|Rd|Boulevard|Blvd|Lane|Ln|Drive|Dr|Court|Ct|Circle|Cir)\\b",
            Pattern.CASE_INSENSITIVE
        ));

        // Date of Birth patterns
        PII_PATTERNS.put("DOB", Pattern.compile(
            "\\b(?:0[1-9]|1[0-2])[/-](?:0[1-9]|[12][0-9]|3[01])[/-](?:19|20)\\d{2}\\b"
        ));

        // Names with common patterns (Mr., Mrs., Ms., Dr. followed by capitalized words)
        PII_PATTERNS.put("NAME", Pattern.compile(
            "\\b(?:Mr\\.|Mrs\\.|Ms\\.|Dr\\.|Prof\\.)\\s+[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b"
        ));
    }

    public String redactPii(String text) {
        if (!piiRedactionEnabled || text == null || text.isEmpty()) {
            return text;
        }

        String redactedText = text;
        Map<String, Integer> redactionCounts = new HashMap<>();

        for (Map.Entry<String, Pattern> entry : PII_PATTERNS.entrySet()) {
            String piiType = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(redactedText);

            int count = 0;
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                count++;
                matcher.appendReplacement(sb, "[" + piiType + "]");
            }
            matcher.appendTail(sb);

            if (count > 0) {
                redactedText = sb.toString();
                redactionCounts.put(piiType, count);
            }
        }

        if (!redactionCounts.isEmpty()) {
            log.info("PII Redaction performed: {}", redactionCounts);
        }

        return redactedText;
    }

    public boolean containsPii(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (Pattern pattern : PII_PATTERNS.values()) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Integer> detectPiiTypes(String text) {
        Map<String, Integer> detectedPii = new HashMap<>();

        if (text == null || text.isEmpty()) {
            return detectedPii;
        }

        for (Map.Entry<String, Pattern> entry : PII_PATTERNS.entrySet()) {
            String piiType = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(text);

            int count = 0;
            while (matcher.find()) {
                count++;
            }

            if (count > 0) {
                detectedPii.put(piiType, count);
            }
        }

        return detectedPii;
    }
}

