package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentUploadResponseTest {

    @Test
    void builder_shouldCreateValidObject() {
        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .documentId("doc123")
                .fileName("test.pdf")
                .version("1.0")
                .success(true)
                .message("Success")
                .pagesProcessed(10)
                .chunksCreated(50)
                .build();

        assertThat(response.getDocumentId()).isEqualTo("doc123");
        assertThat(response.getFileName()).isEqualTo("test.pdf");
        assertThat(response.getVersion()).isEqualTo("1.0");
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getPagesProcessed()).isEqualTo(10);
        assertThat(response.getChunksCreated()).isEqualTo(50);
    }

    @Test
    void settersAndGetters_shouldWork() {
        DocumentUploadResponse response = new DocumentUploadResponse();
        response.setDocumentId("doc456");
        response.setFileName("file.pdf");
        response.setSuccess(true);

        assertThat(response.getDocumentId()).isEqualTo("doc456");
        assertThat(response.getFileName()).isEqualTo("file.pdf");
        assertThat(response.getSuccess()).isTrue();
    }
}

