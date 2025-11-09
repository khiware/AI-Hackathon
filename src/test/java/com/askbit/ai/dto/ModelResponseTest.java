package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelResponseTest {

    @Test
    void builder_shouldCreateValidObject() {
        // Act
        ModelResponse response = ModelResponse.builder()
                .content("Test response")
                .modelUsed("gpt-4")
                .responseTimeMs(1500L)
                .fromCache(false)
                .success(true)
                .error(null)
                .failoverUsed(false)
                .failoverReason(null)
                .build();

        // Assert
        assertThat(response.getContent()).isEqualTo("Test response");
        assertThat(response.getModelUsed()).isEqualTo("gpt-4");
        assertThat(response.getResponseTimeMs()).isEqualTo(1500L);
        assertThat(response.getFromCache()).isFalse();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getError()).isNull();
        assertThat(response.getFailoverUsed()).isFalse();
        assertThat(response.getFailoverReason()).isNull();
    }

    @Test
    void builder_shouldHandleFailoverScenario() {
        // Act
        ModelResponse response = ModelResponse.builder()
                .content("Fallback response")
                .modelUsed("gpt-3.5-turbo")
                .responseTimeMs(800L)
                .fromCache(false)
                .success(true)
                .failoverUsed(true)
                .failoverReason("Primary model timed out")
                .build();

        // Assert
        assertThat(response.getContent()).isEqualTo("Fallback response");
        assertThat(response.getModelUsed()).isEqualTo("gpt-3.5-turbo");
        assertThat(response.getFailoverUsed()).isTrue();
        assertThat(response.getFailoverReason()).isEqualTo("Primary model timed out");
    }

    @Test
    void builder_shouldHandleCachedResponse() {
        // Act
        ModelResponse response = ModelResponse.builder()
                .content("Cached answer")
                .modelUsed("cached")
                .responseTimeMs(0L)
                .fromCache(true)
                .success(true)
                .build();

        // Assert
        assertThat(response.getFromCache()).isTrue();
        assertThat(response.getModelUsed()).isEqualTo("cached");
        assertThat(response.getResponseTimeMs()).isEqualTo(0L);
    }

    @Test
    void builder_shouldHandleErrorResponse() {
        // Act
        ModelResponse response = ModelResponse.builder()
                .content(null)
                .modelUsed("gpt-4")
                .responseTimeMs(5000L)
                .fromCache(false)
                .success(false)
                .error("TIMEOUT: Model took longer than 30000ms")
                .failoverUsed(false)
                .build();

        // Assert
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getError()).contains("TIMEOUT");
        assertThat(response.getContent()).isNull();
    }

    @Test
    void noArgsConstructor_shouldWork() {
        // Act
        ModelResponse response = new ModelResponse();

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void allArgsConstructor_shouldWork() {
        // Act
        ModelResponse response = new ModelResponse(
                "Test content",
                "gpt-4",
                1000L,
                false,
                true,
                null,
                false,
                null
        );

        // Assert
        assertThat(response.getContent()).isEqualTo("Test content");
        assertThat(response.getModelUsed()).isEqualTo("gpt-4");
        assertThat(response.getResponseTimeMs()).isEqualTo(1000L);
    }

    @Test
    void settersAndGetters_shouldWork() {
        // Arrange
        ModelResponse response = new ModelResponse();

        // Act
        response.setContent("New content");
        response.setModelUsed("gpt-3.5");
        response.setResponseTimeMs(500L);
        response.setFromCache(true);
        response.setSuccess(true);
        response.setError(null);
        response.setFailoverUsed(false);
        response.setFailoverReason(null);

        // Assert
        assertThat(response.getContent()).isEqualTo("New content");
        assertThat(response.getModelUsed()).isEqualTo("gpt-3.5");
        assertThat(response.getResponseTimeMs()).isEqualTo(500L);
        assertThat(response.getFromCache()).isTrue();
    }

    @Test
    void equals_shouldWorkCorrectly() {
        // Arrange
        ModelResponse response1 = ModelResponse.builder()
                .content("Same content")
                .modelUsed("gpt-4")
                .success(true)
                .build();

        ModelResponse response2 = ModelResponse.builder()
                .content("Same content")
                .modelUsed("gpt-4")
                .success(true)
                .build();

        // Assert
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Arrange
        ModelResponse response = ModelResponse.builder()
                .content("Test")
                .modelUsed("gpt-4")
                .build();

        // Act
        int hash1 = response.hashCode();
        int hash2 = response.hashCode();

        // Assert
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void toString_shouldWork() {
        // Arrange
        ModelResponse response = ModelResponse.builder()
                .content("Test content")
                .modelUsed("gpt-4")
                .success(true)
                .build();

        // Act
        String result = response.toString();

        // Assert
        assertThat(result).contains("Test content");
        assertThat(result).contains("gpt-4");
    }
}

