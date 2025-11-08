package com.askbit.ai;

import com.askbit.ai.service.PiiRedactionService;
import com.askbit.ai.service.QuestionAnsweringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application.properties")
class AskBitAiApplicationTests {

    @Autowired(required = false)
    private QuestionAnsweringService questionAnsweringService;

    @Autowired(required = false)
    private PiiRedactionService piiRedactionService;

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
        assertNotNull(piiRedactionService, "PiiRedactionService should be loaded");
    }

    @Test
    void testPiiRedaction() {
        if (piiRedactionService == null) {
            return; // Skip if service not available
        }

        String textWithPii = "Contact me at john.doe@example.com or call 555-123-4567";
        String redacted = piiRedactionService.redactPii(textWithPii);

        assertFalse(redacted.contains("john.doe@example.com"), "Email should be redacted");
        assertTrue(redacted.contains("[EMAIL]"), "Should contain EMAIL placeholder");
    }

    @Test
    void testPiiDetection() {
        if (piiRedactionService == null) {
            return; // Skip if service not available
        }

        String textWithPii = "My email is test@company.com";
        boolean containsPii = piiRedactionService.containsPii(textWithPii);

        assertTrue(containsPii, "Should detect email as PII");
    }

    @Test
    void testNoPiiInCleanText() {
        if (piiRedactionService == null) {
            return; // Skip if service not available
        }

        String cleanText = "What is the company PTO policy?";
        boolean containsPii = piiRedactionService.containsPii(cleanText);

        assertFalse(containsPii, "Clean text should not contain PII");
    }
}

