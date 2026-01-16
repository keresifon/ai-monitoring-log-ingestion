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
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LogController
 */
@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogIngestionService logIngestionService;

    @Test
    void shouldIngestLogSuccessfully() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .timestamp(Instant.now())
                .level("INFO")
                .message("Test log message")
                .service("test-service")
                .host("localhost")
                .build();

        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id-123")
                .status("accepted")
                .timestamp(Instant.now())
                .message("Log entry accepted for processing")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRejectLogWithMissingLevel() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .message("Test log message")
                .service("test-service")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.level").exists());
    }

    @Test
    void shouldRejectLogWithInvalidLevel() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INVALID")
                .message("Test log message")
                .service("test-service")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.level").exists());
    }

    @Test
    void shouldRejectLogWithMissingMessage() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .service("test-service")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void shouldRejectLogWithMissingService() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test log message")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.service").exists());
    }

    @Test
    void shouldRejectLogWithMessageTooLong() throws Exception {
        // Arrange
        String longMessage = "a".repeat(10001);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message(longMessage)
                .service("test-service")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void shouldAcceptLogWithMetadata() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("ERROR")
                .message("Test error message")
                .service("test-service")
                .metadata(new HashMap<>() {{
                    put("errorCode", "ERR_001");
                    put("userId", "user123");
                }})
                .build();

        LogResponseDTO response = LogResponseDTO.builder()
                .id("test-id-456")
                .status("accepted")
                .timestamp(Instant.now())
                .message("Log entry accepted for processing")
                .build();

        when(logIngestionService.ingestLog(any(LogEntryDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("test-id-456"))
                .andExpect(jsonPath("$.status").value("accepted"));
    }

    @Test
    void shouldReturnHealthStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/logs/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("log-ingestion"));
    }
}

// Made with Bob
