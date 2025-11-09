package com.askbit.ai.analyzer;

import com.askbit.ai.dto.TemporalContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to analyze queries for temporal/version-specific information
 */
@Service
@Slf4j
public class TemporalQueryAnalyzer {

    // Patterns to detect year references
    private static final Pattern YEAR_PATTERN = Pattern.compile(
            "\\b(20\\d{2})\\b|\\b(FY\\s*20\\d{2}[-–]?\\d{0,2})\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patterns to detect temporal keywords
    private static final Pattern TEMPORAL_KEYWORDS = Pattern.compile(
            "\\b(previous|old|older|earlier|before|past|last year|" +
            "historic|historical|former|prior|in 20\\d{2})\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patterns for "current" or "latest"
    private static final Pattern CURRENT_KEYWORDS = Pattern.compile(
            "\\b(current|latest|new|newest|recent|now|today|this year|updated)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Analyze a question for temporal/version context
     */
    public TemporalContext analyzeQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return TemporalContext.latest();
        }

        String normalizedQuestion = question.trim();

        // Check for explicit year references
        Matcher yearMatcher = YEAR_PATTERN.matcher(normalizedQuestion);
        if (yearMatcher.find()) {
            String yearStr = yearMatcher.group(1);
            if (yearStr == null) {
                yearStr = yearMatcher.group(2); // FY format
                // Extract year from FY format (e.g., "FY 2023-24" -> "2023")
                yearStr = yearStr.replaceAll("[^0-9]", "").substring(0, 4);
            }

            int year = Integer.parseInt(yearStr);
            int currentYear = Year.now().getValue();

            log.info("Detected explicit year reference: {}", year);

            if (year < currentYear) {
                return TemporalContext.forYear(year);
            } else {
                return TemporalContext.latest();
            }
        }

        // Check for temporal keywords indicating older versions
        Matcher temporalMatcher = TEMPORAL_KEYWORDS.matcher(normalizedQuestion);
        if (temporalMatcher.find()) {
            log.info("Detected temporal keyword: {}", temporalMatcher.group());
            // If asking about old/previous, but no specific year, we need clarification
            return TemporalContext.needsClarification();
        }

        // Check for explicit "current" or "latest" keywords
        Matcher currentMatcher = CURRENT_KEYWORDS.matcher(normalizedQuestion);
        if (currentMatcher.find()) {
            log.info("Detected current/latest keyword: {}", currentMatcher.group());
            return TemporalContext.latest();
        }

        // Default: use latest version
        return TemporalContext.latest();
    }
}

