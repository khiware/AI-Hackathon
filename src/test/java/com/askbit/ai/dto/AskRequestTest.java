package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AskRequestTest {

    @Test
    void builder_shouldCreateValidObject() {
        AskRequest request = AskRequest.builder()
                .question("What is the WFH policy?")
                .context("Previous discussion")
                .build();

        assertThat(request.getQuestion()).isEqualTo("What is the WFH policy?");
        assertThat(request.getContext()).isEqualTo("Previous discussion");
    }

    @Test
    void settersAndGetters_shouldWork() {
        AskRequest request = new AskRequest();
        request.setQuestion("Test question");
        request.setContext("Test context");

        assertThat(request.getQuestion()).isEqualTo("Test question");
        assertThat(request.getContext()).isEqualTo("Test context");
    }
}

