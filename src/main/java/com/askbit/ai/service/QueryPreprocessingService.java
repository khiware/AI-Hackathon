package com.askbit.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service to preprocess user queries by handling spelling mistakes, special characters,
 * and refactoring questions for better understanding.
 */
@Service
@Slf4j
public class QueryPreprocessingService {

    // Common spelling mistakes in HR/policy domain
    private static final Map<String, String> SPELLING_CORRECTIONS = new HashMap<>();

    // Common abbreviations and their expansions
    private static final Map<String, String> ABBREVIATION_EXPANSIONS = new HashMap<>();

    static {
        // Spelling corrections
        SPELLING_CORRECTIONS.put("increament", "increment");
        SPELLING_CORRECTIONS.put("incremnet", "increment");
        SPELLING_CORRECTIONS.put("sallary", "salary");
        SPELLING_CORRECTIONS.put("salery", "salary");
        SPELLING_CORRECTIONS.put("polcy", "policy");
        SPELLING_CORRECTIONS.put("policey", "policy");
        SPELLING_CORRECTIONS.put("employe", "employee");
        SPELLING_CORRECTIONS.put("employeee", "employee");
        SPELLING_CORRECTIONS.put("benifits", "benefits");
        SPELLING_CORRECTIONS.put("benifit", "benefit");
        SPELLING_CORRECTIONS.put("variabel", "variable");
        SPELLING_CORRECTIONS.put("varible", "variable");
        SPELLING_CORRECTIONS.put("reimbursment", "reimbursement");
        SPELLING_CORRECTIONS.put("reimbursement", "reimbursement");
        SPELLING_CORRECTIONS.put("probation", "probation");
        SPELLING_CORRECTIONS.put("probationn", "probation");
        SPELLING_CORRECTIONS.put("anual", "annual");
        SPELLING_CORRECTIONS.put("annuel", "annual");
        SPELLING_CORRECTIONS.put("payed", "paid");
        SPELLING_CORRECTIONS.put("paied", "paid");
        SPELLING_CORRECTIONS.put("flexnext", "FlexNext");
        SPELLING_CORRECTIONS.put("flextrack", "FlexTrack");
        SPELLING_CORRECTIONS.put("progresive", "progressive");
        SPELLING_CORRECTIONS.put("progressiv", "progressive");
        SPELLING_CORRECTIONS.put("quaterly", "quarterly");
        SPELLING_CORRECTIONS.put("quater", "quarter");
        SPELLING_CORRECTIONS.put("eligable", "eligible");
        SPELLING_CORRECTIONS.put("eligibility", "eligibility");
        SPELLING_CORRECTIONS.put("elligible", "eligible");
        SPELLING_CORRECTIONS.put("appraisel", "appraisal");
        SPELLING_CORRECTIONS.put("appraisal", "appraisal");
        SPELLING_CORRECTIONS.put("seperation", "separation");
        SPELLING_CORRECTIONS.put("seperation", "separation");
        SPELLING_CORRECTIONS.put("releiving", "relieving");
        SPELLING_CORRECTIONS.put("reliving", "relieving");
        SPELLING_CORRECTIONS.put("maturnity", "maternity");
        SPELLING_CORRECTIONS.put("paturnity", "paternity");
        SPELLING_CORRECTIONS.put("vaccation", "vacation");
        SPELLING_CORRECTIONS.put("vacations", "vacation");
        SPELLING_CORRECTIONS.put("absense", "absence");
        SPELLING_CORRECTIONS.put("absance", "absence");
        SPELLING_CORRECTIONS.put("attendence", "attendance");
        SPELLING_CORRECTIONS.put("attendace", "attendance");
        SPELLING_CORRECTIONS.put("reimberse", "reimburse");
        SPELLING_CORRECTIONS.put("performace", "performance");
        SPELLING_CORRECTIONS.put("performence", "performance");

        // Abbreviations
        ABBREVIATION_EXPANSIONS.put("pvp", "progressive variable pay");
        ABBREVIATION_EXPANSIONS.put("p.v.p", "progressive variable pay");
        ABBREVIATION_EXPANSIONS.put("vp", "variable pay");
        ABBREVIATION_EXPANSIONS.put("v.p", "variable pay");
        ABBREVIATION_EXPANSIONS.put("hr", "human resources");
        ABBREVIATION_EXPANSIONS.put("h.r", "human resources");
        ABBREVIATION_EXPANSIONS.put("fy", "fiscal year");
        ABBREVIATION_EXPANSIONS.put("f.y", "fiscal year");
        ABBREVIATION_EXPANSIONS.put("wfh", "work from home");
        ABBREVIATION_EXPANSIONS.put("w.f.h", "work from home");
        ABBREVIATION_EXPANSIONS.put("lta", "leave travel allowance");
        ABBREVIATION_EXPANSIONS.put("l.t.a", "leave travel allowance");
        ABBREVIATION_EXPANSIONS.put("hra", "house rent allowance");
        ABBREVIATION_EXPANSIONS.put("h.r.a", "house rent allowance");
        ABBREVIATION_EXPANSIONS.put("pl", "privilege leave");
        ABBREVIATION_EXPANSIONS.put("p.l", "privilege leave");
        ABBREVIATION_EXPANSIONS.put("cl", "casual leave");
        ABBREVIATION_EXPANSIONS.put("c.l", "casual leave");
        ABBREVIATION_EXPANSIONS.put("sl", "sick leave");
        ABBREVIATION_EXPANSIONS.put("s.l", "sick leave");
        ABBREVIATION_EXPANSIONS.put("ctc", "cost to company");
        ABBREVIATION_EXPANSIONS.put("c.t.c", "cost to company");
        ABBREVIATION_EXPANSIONS.put("pf", "provident fund");
        ABBREVIATION_EXPANSIONS.put("p.f", "provident fund");
        ABBREVIATION_EXPANSIONS.put("epf", "employee provident fund");
        ABBREVIATION_EXPANSIONS.put("e.p.f", "employee provident fund");
        ABBREVIATION_EXPANSIONS.put("esi", "employee state insurance");
        ABBREVIATION_EXPANSIONS.put("e.s.i", "employee state insurance");
        ABBREVIATION_EXPANSIONS.put("nda", "non-disclosure agreement");
        ABBREVIATION_EXPANSIONS.put("n.d.a", "non-disclosure agreement");
    }

    /**
     * Preprocess the user's question by ONLY fixing spelling and grammar mistakes.
     * Other transformations like abbreviation expansion are NOT applied unless there's an error.
     *
     * @param question Original user question
     * @return Preprocessed question (only if errors were found)
     */
    public String preprocessQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return question;
        }

        log.debug("Original question: {}", question);

        String processed = question;

        // Step 1: Normalize whitespace (excessive spaces are grammar errors)
        processed = normalizeWhitespace(processed);

        // Step 2: Clean excessive special characters (considered errors)
        processed = cleanSpecialCharacters(processed);

        // Step 3: Fix common spelling mistakes ONLY
        processed = correctSpellingMistakes(processed);

        // Step 4: Fix basic grammar issues (missing question marks, etc.)
        processed = fixBasicGrammar(processed);

        // Step 5: Final cleanup
        processed = processed.trim();

        if (!question.equals(processed)) {
            log.info("Question corrected: '{}' -> '{}'", question, processed);
        }

        return processed;
    }

    /**
     * Normalize whitespace - remove extra spaces, tabs, newlines
     */
    private String normalizeWhitespace(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Clean special characters while preserving meaningful punctuation
     */
    private String cleanSpecialCharacters(String text) {
        // Remove repeated punctuation (e.g., "???" -> "?")
        text = text.replaceAll("([!?.,;:]){2,}", "$1");

        // Remove special characters that might cause issues, but keep apostrophes, hyphens, and basic punctuation
        text = text.replaceAll("[^a-zA-Z0-9\\s'\\-.,?!:/]", "");

        // Remove trailing special characters before spaces
        text = text.replaceAll("([.,;:])\\s+([.,;:])", "$2");

        // Ensure space after punctuation for better readability
        text = text.replaceAll("([.,!?])([a-zA-Z])", "$1 $2");

        return text;
    }

    /**
     * Expand common abbreviations to their full forms
     */
    private String expandAbbreviations(String text) {
        String result = text;

        // Process abbreviations (case-insensitive)
        for (Map.Entry<String, String> entry : ABBREVIATION_EXPANSIONS.entrySet()) {
            String abbr = entry.getKey();
            String expansion = entry.getValue();

            // Match abbreviation as whole word (with word boundaries)
            // Case-insensitive matching
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(abbr) + "\\b", Pattern.CASE_INSENSITIVE);
            result = pattern.matcher(result).replaceAll(expansion);
        }

        return result;
    }

    /**
     * Correct common spelling mistakes
     */
    private String correctSpellingMistakes(String text) {
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String lowerWord = word.toLowerCase();

            // Check if this word (without punctuation) needs correction
            String wordWithoutPunct = lowerWord.replaceAll("[^a-z0-9]", "");

            if (SPELLING_CORRECTIONS.containsKey(wordWithoutPunct)) {
                // Preserve original case pattern
                String correction = SPELLING_CORRECTIONS.get(wordWithoutPunct);
                String corrected = preserveCase(word, correction, lowerWord, wordWithoutPunct);
                result.append(corrected);
            } else {
                result.append(word);
            }

            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    /**
     * Preserve the original case pattern when correcting spelling
     */
    private String preserveCase(String original, String correction, String lowerOriginal, String wordWithoutPunct) {
        // Get any punctuation from the original word
        String punct = original.replaceAll("[a-zA-Z0-9]", "");

        // If original started with uppercase, capitalize correction
        if (!original.isEmpty() && Character.isUpperCase(original.charAt(0))) {
            correction = correction.substring(0, 1).toUpperCase() + correction.substring(1);
        }

        return correction + punct;
    }

    /**
     * Fix basic grammar issues (only actual errors, not transformations)
     */
    private String fixBasicGrammar(String text) {
        String result = text;

        // Fix common typos in question words (these are spelling errors)
        result = result.replaceAll("(?i)^wat\\s", "what ");
        result = result.replaceAll("(?i)^wht\\s", "what ");
        result = result.replaceAll("(?i)^wt\\s", "what ");
        result = result.replaceAll("(?i)^wen\\s", "when ");
        result = result.replaceAll("(?i)^whn\\s", "when ");
        result = result.replaceAll("(?i)^wer\\s", "where ");
        result = result.replaceAll("(?i)^wre\\s", "where ");
        result = result.replaceAll("(?i)^hw\\s", "how ");

        // Only add question mark if the sentence looks like a question but is missing one
        // (starts with question word but no ending punctuation)
        if (result.matches("(?i)^(what|when|where|who|why|how|can|could|is|are|does|do|will|would)\\b.*")
            && !result.trim().endsWith("?")
            && !result.trim().endsWith(".")
            && !result.trim().endsWith("!")) {
            result = result.trim() + "?";
        }

        return result;
    }

    /**
     * Get information about what was corrected in the preprocessing
     * Useful for debugging or showing users what was understood
     */
    public String getPreprocessingInfo(String original, String processed) {
        if (original.equals(processed)) {
            return "No changes made";
        }

        return String.format("Original: '%s' -> Processed: '%s'", original, processed);
    }
}

