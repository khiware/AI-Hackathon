package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AskResponseTest {

    @Test
    void builder_shouldCreateValidObject() {
        AskResponse response = AskResponse.builder()
                .answer("Test answer")
                .confidence(0.95)
                .cached(false)
                .build();

        assertThat(response.getAnswer()).isEqualTo("Test answer");
        assertThat(response.getConfidence()).isEqualTo(0.95);
        assertThat(response.getCached()).isFalse();
    }

    @Test
    void settersAndGetters_shouldWork() {
        AskResponse response = new AskResponse();
        response.setAnswer("Answer");
        response.setConfidence(0.8);

        assertThat(response.getAnswer()).isEqualTo("Answer");
        assertThat(response.getConfidence()).isEqualTo(0.8);
    }
}

