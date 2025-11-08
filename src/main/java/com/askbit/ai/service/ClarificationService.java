package com.askbit.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ClarificationService {

    private static final Map<String, List<String>> AMBIGUOUS_TERMS = new HashMap<>();
    private static final Set<String> VAGUE_QUESTIONS = new HashSet<>();

    static {
        // Define ambiguous terms and their clarification options
        AMBIGUOUS_TERMS.put("pto", List.of(
                "full-time employees in the US",
                "part-time employees",
                "contractors",
                "international employees"
        ));

        AMBIGUOUS_TERMS.put("vacation", List.of(
                "annual vacation days",
                "sick leave",
                "personal days",
                "unpaid leave"
        ));

        AMBIGUOUS_TERMS.put("expense", List.of(
                "travel expenses",
                "equipment expenses",
                "meal expenses",
                "training expenses"
        ));

        AMBIGUOUS_TERMS.put("remote work", List.of(
                "full-time remote",
                "hybrid work",
                "temporary remote work",
                "international remote work"
        ));

        AMBIGUOUS_TERMS.put("benefit", List.of(
                "health insurance",
                "retirement benefits",
                "stock options",
                "education benefits"
        ));

        // Vague question patterns
        VAGUE_QUESTIONS.add("what is the policy");
        VAGUE_QUESTIONS.add("tell me about");
        VAGUE_QUESTIONS.add("how does it work");
        VAGUE_QUESTIONS.add("can you explain");
    }

    public boolean needsClarification(String question) {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }

        String normalizedQuestion = question.toLowerCase().trim();

        // Check if question is too vague
        if (isVagueQuestion(normalizedQuestion)) {
            return true;
        }

        // Check if question contains ambiguous terms without context
        return containsAmbiguousTerm(normalizedQuestion) &&
               !hasSpecificContext(normalizedQuestion);
    }

    public String generateClarificationQuestion(String question) {
        String normalizedQuestion = question.toLowerCase().trim();

        // Find the ambiguous term
        for (Map.Entry<String, List<String>> entry : AMBIGUOUS_TERMS.entrySet()) {
            String term = entry.getKey();
            if (normalizedQuestion.contains(term)) {
                List<String> options = entry.getValue();
                return buildClarificationQuestion(term, options);
            }
        }

        // Generic clarification for vague questions
        if (isVagueQuestion(normalizedQuestion)) {
            return "Could you please be more specific? For example, are you asking about:\n" +
                   "- Employee policies (PTO, benefits, expenses)\n" +
                   "- Work arrangements (remote work, office hours)\n" +
                   "- Compensation and benefits\n" +
                   "- Company processes and procedures";
        }

        return "Could you please provide more details about your question?";
    }

    private boolean isVagueQuestion(String question) {
        for (String vague : VAGUE_QUESTIONS) {
            if (question.startsWith(vague)) {
                return true;
            }
        }

        // Check if question is too short (likely too vague)
        String[] words = question.split("\\s+");
        return words.length < 4;
    }

    private boolean containsAmbiguousTerm(String question) {
        for (String term : AMBIGUOUS_TERMS.keySet()) {
            if (question.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpecificContext(String question) {
        // Check for specific context indicators
        List<String> contextIndicators = Arrays.asList(
                "full-time", "part-time", "contractor",
                "us", "international", "remote", "office",
                "annual", "sick", "personal",
                "travel", "equipment", "training"
        );

        for (String indicator : contextIndicators) {
            if (question.contains(indicator)) {
                return true;
            }
        }

        return false;
    }

    private String buildClarificationQuestion(String term, List<String> options) {
        StringBuilder clarification = new StringBuilder();
        clarification.append("I found information about '")
                .append(term)
                .append("', but I need more context. Are you asking about:\n");

        for (int i = 0; i < options.size(); i++) {
            clarification.append((i + 1))
                    .append(". ")
                    .append(options.get(i))
                    .append("\n");
        }

        clarification.append("\nPlease specify which one you're interested in.");

        return clarification.toString();
    }

    public String expandQuestionWithContext(String originalQuestion, String clarification) {
        // Combine original question with clarification context
        return originalQuestion + " (specifically about: " + clarification + ")";
    }
}

