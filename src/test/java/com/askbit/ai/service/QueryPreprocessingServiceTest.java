package com.askbit.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for QueryPreprocessingService
 */
class QueryPreprocessingServiceTest {

    private QueryPreprocessingService queryPreprocessingService;

    @BeforeEach
    void setUp() {
        queryPreprocessingService = new QueryPreprocessingService();
    }

    @Test
    void testSpellingCorrection() {
        // Test common spelling mistakes
        String input = "What is the increament polcy?";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.contains("increment"));
        assertTrue(result.contains("policy"));
    }

    @Test
    void testAbbreviationExpansion() {
        // Test abbreviation expansion
        String input = "What is pvp?";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.toLowerCase().contains("progressive variable pay"));
    }

    @Test
    void testSpecialCharacterCleaning() {
        // Test special character removal
        String input = "What is the sallary??? @#$";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertFalse(result.contains("@#$"));
        assertTrue(result.contains("salary"));
        assertFalse(result.contains("???"));
    }

    @Test
    void testTextSpeakConversion() {
        // Test text speak to proper words
        String input = "pls tell me abt flexnext";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.contains("please"));
        assertTrue(result.contains("FlexNext"));
    }

    @Test
    void testQuestionMarkAddition() {
        // Test automatic question mark addition
        String input = "What is the leave policy";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.endsWith("?"));
    }

    @Test
    void testWhitespaceNormalization() {
        // Test whitespace normalization
        String input = "What    is     the    policy";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertFalse(result.contains("    "));
        assertEquals("What is the policy?", result);
    }

    @Test
    void testCombinedProcessing() {
        // Test multiple corrections at once
        String input = "wat is the increament polcy for employe???";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.contains("what"));
        assertTrue(result.contains("increment"));
        assertTrue(result.contains("policy"));
        assertTrue(result.contains("employee"));
        assertFalse(result.contains("???"));
    }

    @Test
    void testEmptyQuestion() {
        // Test empty question handling
        String input = "";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertEquals("", result);
    }

    @Test
    void testNullQuestion() {
        // Test null question handling
        String result = queryPreprocessingService.preprocessQuestion(null);
        assertNull(result);
    }

    @Test
    void testMultipleAbbreviations() {
        // Test multiple abbreviations in one question
        String input = "What is hr policy for wfh and pf?";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.toLowerCase().contains("human resources"));
        assertTrue(result.toLowerCase().contains("work from home"));
        assertTrue(result.toLowerCase().contains("provident fund"));
    }

    @Test
    void testCasePreservation() {
        // Test that proper capitalization is preserved
        String input = "What is FlexNext polcy?";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.contains("FlexNext"));
        assertTrue(result.contains("policy"));
    }

    @Test
    void testInformalPhraseConversion() {
        // Test conversion of informal phrases
        String input = "can u tell me about the leave policy";
        String result = queryPreprocessingService.preprocessQuestion(input);
        assertTrue(result.toLowerCase().contains("what is"));
        assertFalse(result.contains("can u tell"));
    }
}

