package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MetricsResponseTest {

    @Test
    void builder_shouldCreateValidObject() {
        MetricsResponse response = MetricsResponse.builder()
                .totalQueries(1000L)
                .averageResponseTimeMs(1500.0)
                .cacheHitRate(0.75)
                .mostUsedModel("gpt-4")
                .build();

        assertThat(response.getTotalQueries()).isEqualTo(1000L);
        assertThat(response.getAverageResponseTimeMs()).isEqualTo(1500.0);
        assertThat(response.getCacheHitRate()).isEqualTo(0.75);
        assertThat(response.getMostUsedModel()).isEqualTo("gpt-4");
    }

    @Test
    void settersAndGetters_shouldWork() {
        MetricsResponse response = new MetricsResponse();
        response.setTotalQueries(500L);
        response.setMostUsedModel("gpt-3.5");

        assertThat(response.getTotalQueries()).isEqualTo(500L);
        assertThat(response.getMostUsedModel()).isEqualTo("gpt-3.5");
    }
}

