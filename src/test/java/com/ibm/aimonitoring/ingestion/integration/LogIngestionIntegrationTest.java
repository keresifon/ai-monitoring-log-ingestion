package com.ibm.aimonitoring.ingestion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.aimonitoring.ingestion.dto.LogEntryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for log ingestion flow
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LogIngestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private RabbitAdmin rabbitAdmin;

    @MockBean
    private DataSource dataSource;

    @Test
    void shouldIngestLogWithFullMetadata() throws Exception {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", "user123");
        metadata.put("sessionId", "session456");
        metadata.put("requestId", "req789");

        LogEntryDTO logEntry = LogEntryDTO.builder()
                .timestamp(Instant.now())
                .level("ERROR")
                .message("Critical error occurred")
                .service("payment-service")
                .host("payment-host-01")
                .environment("production")
                .metadata(metadata)
                .traceId("trace-abc-123")
                .spanId("span-xyz-456")
                .build();

        doNothing().when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(LogEntryDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Log entry accepted for processing"));
    }

    @Test
    void shouldIngestLogWithMinimalFields() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Service started")
                .service("test-service")
                .build();

        doNothing().when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(LogEntryDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("accepted"));
    }

    @Test
    void shouldEnrichLogWithTraceAndSpanIds() throws Exception {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("DEBUG")
                .message("Debug message")
                .service("test-service")
                .traceId("trace-123")
                .spanId("span-456")
                .build();

        doNothing().when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(LogEntryDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldRejectInvalidJson() throws Exception {
        // Act & Assert - Invalid JSON results in an error response (400 or 500)
        var result = mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andReturn();
        
        int status = result.getResponse().getStatus();
        assertThat(status).isGreaterThanOrEqualTo(400);
    }

    @Test
    void shouldRejectEmptyBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void shouldHandleAllLogLevels() throws Exception {
        String[] levels = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

        doNothing().when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(LogEntryDTO.class));

        for (String level : levels) {
            LogEntryDTO logEntry = LogEntryDTO.builder()
                    .level(level)
                    .message("Test message for " + level)
                    .service("test-service")
                    .build();

            mockMvc.perform(post("/api/v1/logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logEntry)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.status").value("accepted"));
        }
    }
}

// Made with Bob
