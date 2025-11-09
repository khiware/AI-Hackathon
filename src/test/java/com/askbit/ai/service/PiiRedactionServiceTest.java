package com.askbit.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PiiRedactionServiceTest {

    @InjectMocks
    private PiiRedactionService piiRedactionService;

    @Test
    void redactPii_shouldRedactEmail() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Contact me at john.doe@example.com for details.";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).isEqualTo("Contact me at [EMAIL] for details.");
        assertThat(result).doesNotContain("john.doe@example.com");
    }

    @Test
    void redactPii_shouldRedactPhoneNumber() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Call me at 555-123-4567";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[PHONE]");
        assertThat(result).doesNotContain("555-123-4567");
    }

    @Test
    void redactPii_shouldRedactSSN() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "My SSN is 123-45-6789";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[SSN]");
        assertThat(result).doesNotContain("123-45-6789");
    }

    @Test
    void redactPii_shouldRedactCreditCard() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Card number: 4532015112830366";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[CREDIT_CARD]");
    }

    @Test
    void redactPii_shouldRedactIPAddress() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Server IP: 192.168.1.1";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[IP_ADDRESS]");
    }

    @Test
    void redactPii_shouldRedactMultiplePiiTypes() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Contact john.doe@example.com or call 555-123-4567";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[EMAIL]");
        assertThat(result).contains("[PHONE]");
    }

    @Test
    void redactPii_shouldReturnOriginalWhenDisabled() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", false);
        String text = "Email: john.doe@example.com";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).isEqualTo(text);
    }

    @Test
    void redactPii_shouldReturnNullForNull() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);

        // Act
        String result = piiRedactionService.redactPii(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void redactPii_shouldReturnEmptyForEmpty() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);

        // Act
        String result = piiRedactionService.redactPii("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void containsPii_shouldReturnTrueForEmail() {
        // Arrange
        String text = "Email: test@example.com";

        // Act
        boolean result = piiRedactionService.containsPii(text);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void containsPii_shouldReturnTrueForPhone() {
        // Arrange
        String text = "Phone: 555-123-4567";

        // Act
        boolean result = piiRedactionService.containsPii(text);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void containsPii_shouldReturnFalseForCleanText() {
        // Arrange
        String text = "This is clean text without any PII.";

        // Act
        boolean result = piiRedactionService.containsPii(text);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void containsPii_shouldReturnFalseForNull() {
        // Act
        boolean result = piiRedactionService.containsPii(null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void containsPii_shouldReturnFalseForEmpty() {
        // Act
        boolean result = piiRedactionService.containsPii("");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void detectPiiTypes_shouldDetectEmailAndPhone() {
        // Arrange
        String text = "Contact john@example.com or 555-123-4567";

        // Act
        Map<String, Integer> result = piiRedactionService.detectPiiTypes(text);

        // Assert
        assertThat(result).containsKey("EMAIL");
        assertThat(result).containsKey("PHONE");
        assertThat(result.get("EMAIL")).isEqualTo(1);
        assertThat(result.get("PHONE")).isEqualTo(1);
    }

    @Test
    void detectPiiTypes_shouldReturnEmptyForCleanText() {
        // Arrange
        String text = "Clean text";

        // Act
        Map<String, Integer> result = piiRedactionService.detectPiiTypes(text);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void detectPiiTypes_shouldReturnEmptyForNull() {
        // Act
        Map<String, Integer> result = piiRedactionService.detectPiiTypes(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void detectPiiTypes_shouldCountMultipleOccurrences() {
        // Arrange
        String text = "Email1: john@example.com, Email2: jane@example.com";

        // Act
        Map<String, Integer> result = piiRedactionService.detectPiiTypes(text);

        // Assert
        assertThat(result.get("EMAIL")).isEqualTo(2);
    }

    @Test
    void redactPii_shouldRedactName() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Contact Dr. John Smith";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[NAME]");
    }

    @Test
    void redactPii_shouldRedactDOB() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Born on 01/15/1990";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).contains("[DOB]");
    }

    @Test
    void redactPii_shouldHandleMultipleEmailAddresses() {
        // Arrange
        ReflectionTestUtils.setField(piiRedactionService, "piiRedactionEnabled", true);
        String text = "Contact john@example.com or jane@example.com";

        // Act
        String result = piiRedactionService.redactPii(text);

        // Assert
        assertThat(result).isEqualTo("Contact [EMAIL] or [EMAIL]");
    }
}

