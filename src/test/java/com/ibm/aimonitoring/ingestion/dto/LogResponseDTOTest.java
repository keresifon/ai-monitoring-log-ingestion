package com.ibm.aimonitoring.ingestion.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LogResponseDTO
 */
class LogResponseDTOTest {

    @Test
    void shouldBuildLogResponseWithBuilder() {
        // Arrange
        Instant now = Instant.now();
        
        // Act
        LogResponseDTO response = LogResponseDTO.builder()
                .id("log-123")
                .status("accepted")
                .timestamp(now)
                .message("Log entry accepted for processing")
                .build();

        // Assert
        assertThat(response.getId()).isEqualTo("log-123");
        assertThat(response.getStatus()).isEqualTo("accepted");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getMessage()).isEqualTo("Log entry accepted for processing");
    }

    @Test
    void shouldCreateLogResponseWithAllFields() {
        // Arrange & Act
        Instant timestamp = Instant.parse("2024-01-15T10:00:00Z");
        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id-456")
                .status("processed")
                .timestamp(timestamp)
                .message("Successfully processed")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getMessage()).isNotNull();
    }

    @Test
    void shouldSupportNoArgsConstructor() {
        // Act
        LogResponseDTO response = new LogResponseDTO();
        response.setId("id-789");
        response.setStatus("pending");
        response.setTimestamp(Instant.now());
        response.setMessage("Pending processing");

        // Assert
        assertThat(response.getId()).isEqualTo("id-789");
        assertThat(response.getStatus()).isEqualTo("pending");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Pending processing");
    }

    @Test
    void shouldSupportAllArgsConstructor() {
        // Arrange
        Instant now = Instant.now();
        
        // Act
        LogResponseDTO response = new LogResponseDTO(
                "id-999",
                "completed",
                now,
                "Processing completed"
        );

        // Assert
        assertThat(response.getId()).isEqualTo("id-999");
        assertThat(response.getStatus()).isEqualTo("completed");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getMessage()).isEqualTo("Processing completed");
    }

    @Test
    void shouldHandleNullValues() {
        // Act
        LogResponseDTO response = LogResponseDTO.builder()
                .id(null)
                .status(null)
                .timestamp(null)
                .message(null)
                .build();

        // Assert
        assertThat(response.getId()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getMessage()).isNull();
    }
}

// Made with Bob