package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class TopQuestionResponseTest {

    @Test
    void builder_shouldCreateValidObject() {
        LocalDateTime now = LocalDateTime.now();
        TopQuestionResponse response = TopQuestionResponse.builder()
                .question("What is WFH?")
                .count(50L)
                .avgConfidence(0.92)
                .lastAsked(now)
                .build();

        assertThat(response.getQuestion()).isEqualTo("What is WFH?");
        assertThat(response.getCount()).isEqualTo(50L);
        assertThat(response.getAvgConfidence()).isEqualTo(0.92);
        assertThat(response.getLastAsked()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWork() {
        TopQuestionResponse response = new TopQuestionResponse();
        response.setQuestion("Test");
        response.setCount(10L);

        assertThat(response.getQuestion()).isEqualTo("Test");
        assertThat(response.getCount()).isEqualTo(10L);
    }
}

