package com.ibm.aimonitoring.ingestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.aimonitoring.ingestion.dto.LogEntryDTO;
import com.ibm.aimonitoring.ingestion.dto.LogResponseDTO;
import com.ibm.aimonitoring.ingestion.service.LogIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge case tests for LogController
 */
@WebMvcTest(LogController.class)
class LogControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogIngestionService logIngestionService;

    @Test
    void shouldHandleServiceException() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("ERROR")
                .message("Test message")
                .service("test-service")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class)))
                .thenThrow(new LogIngestionService.LogIngestionException("RabbitMQ connection failed", 
                        new RuntimeException("Connection refused")));

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Log Ingestion Failed"));
    }

    @Test
    void shouldHandleInvalidJson() throws Exception {
        // Act & Assert - Spring returns 500 for malformed JSON
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldHandleEmptyRequestBody() throws Exception {
        // Act & Assert - Spring returns 500 for empty body
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldHandleNullFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void shouldHandleMultipleValidationErrors() throws Exception {
        // Arrange - missing level, message, and service
        String invalidLog = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLog))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldHandleVeryLargeMetadata() throws Exception {
        // Arrange
        StringBuilder largeMetadata = new StringBuilder("{\"level\":\"INFO\",\"message\":\"Test\",\"service\":\"test\",\"metadata\":{");
        for (int i = 0; i < 100; i++) {
            largeMetadata.append("\"key").append(i).append("\":\"value").append(i).append("\"");
            if (i < 99) largeMetadata.append(",");
        }
        largeMetadata.append("}}");

        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id")
                .status("accepted")
                .timestamp(Instant.now())
                .message("Log entry accepted for processing")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(largeMetadata.toString()))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldHandleSpecialCharactersInMessage() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Special chars: <>&\"'\\n\\t\\r")
                .service("test-service")
                .build();

        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id")
                .status("accepted")
                .timestamp(Instant.now())
                .message("Log entry accepted for processing")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldHandleUnicodeCharacters() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Unicode: 你好世界 🚀 émojis")
                .service("test-service")
                .build();

        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id")
                .status("accepted")
                .timestamp(Instant.now())
                .message("Log entry accepted for processing")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldHandleAllLogLevelsSequentially() throws Exception {
        // Arrange
        String[] levels = {"ERROR", "WARN", "INFO", "DEBUG", "TRACE"};
        
        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id")
                .status("accepted")
                .timestamp(Instant.now())
                .message("Log entry accepted for processing")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class))).thenReturn(response);

        // Act & Assert
        for (String level : levels) {
            LogEntryDTO logEntry = LogEntryDTO.builder()
                    .level(level)
                    .message("Test message for " + level)
                    .service("test-service")
                    .build();

            mockMvc.perform(post("/api/v1/logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logEntry)))
                    .andExpect(status().isAccepted());
        }
    }

    @Test
    void shouldHandleRuntimeException() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("ERROR")
                .message("Test message")
                .service("test-service")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}

// Made with Bob