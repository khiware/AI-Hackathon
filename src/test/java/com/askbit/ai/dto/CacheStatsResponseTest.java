package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CacheStatsResponseTest {

    @Test
    void builder_shouldCreateValidObject() {
        CacheStatsResponse response = CacheStatsResponse.builder()
                .size(100L)
                .hitRate(0.75)
                .totalHits(75L)
                .totalMisses(25L)
                .build();

        assertThat(response.getSize()).isEqualTo(100L);
        assertThat(response.getHitRate()).isEqualTo(0.75);
        assertThat(response.getTotalHits()).isEqualTo(75L);
        assertThat(response.getTotalMisses()).isEqualTo(25L);
    }

    @Test
    void settersAndGetters_shouldWork() {
        CacheStatsResponse response = new CacheStatsResponse();
        response.setSize(50L);
        response.setHitRate(0.8);

        assertThat(response.getSize()).isEqualTo(50L);
        assertThat(response.getHitRate()).isEqualTo(0.8);
    }
}

