package com.ibm.aimonitoring.ingestion.service;

import com.ibm.aimonitoring.ingestion.config.RabbitMQConfig;
import com.ibm.aimonitoring.ingestion.dto.LogEntryDTO;
import com.ibm.aimonitoring.ingestion.dto.LogResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Edge case tests for LogIngestionService
 */
@ExtendWith(MockitoExtension.class)
class LogIngestionServiceEdgeCaseTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LogIngestionService logIngestionService;

    private LogEntryDTO testLogEntry;

    @BeforeEach
    void setUp() {
        testLogEntry = LogEntryDTO.builder()
                .timestamp(Instant.now())
                .level("INFO")
                .message("Test log message")
                .service("test-service")
                .host("localhost")
                .environment("test")
                .metadata(new HashMap<>())
                .build();
    }

    @Test
    void shouldHandleRabbitMQConnectionFailure() {
        // Arrange
        doThrow(new AmqpException("Connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act & Assert
        assertThatThrownBy(() -> logIngestionService.ingestLog(testLogEntry))
                .isInstanceOf(LogIngestionService.LogIngestionException.class)
                .hasMessageContaining("Failed to ingest log entry")
                .hasRootCauseInstanceOf(AmqpException.class);
    }

    @Test
    void shouldHandleRabbitMQPublishingFailure() {
        // Arrange
        doThrow(new RuntimeException("Failed to publish message"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act & Assert
        assertThatThrownBy(() -> logIngestionService.ingestLog(testLogEntry))
                .isInstanceOf(LogIngestionService.LogIngestionException.class)
                .hasMessageContaining("Failed to ingest log entry");
    }

    @Test
    void shouldHandleNullMetadata() {
        // Arrange
        testLogEntry.setMetadata(null);
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), logCaptor.capture());

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response).isNotNull();
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getMetadata()).isNotNull();
        assertThat(capturedLog.getMetadata()).containsKey("logId");
        assertThat(capturedLog.getMetadata()).containsKey("ingestedAt");
    }

    @Test
    void shouldHandleEmptyMetadata() {
        // Arrange
        testLogEntry.setMetadata(new HashMap<>());
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), logCaptor.capture());

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response).isNotNull();
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getMetadata()).hasSize(2); // logId and ingestedAt
    }

    @Test
    void shouldPreserveExistingMetadata() {
        // Arrange
        Map<String, Object> existingMetadata = new HashMap<>();
        existingMetadata.put("userId", "user123");
        existingMetadata.put("requestId", "req456");
        testLogEntry.setMetadata(existingMetadata);
        
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getMetadata()).containsKey("userId");
        assertThat(capturedLog.getMetadata()).containsKey("requestId");
        assertThat(capturedLog.getMetadata()).containsKey("logId");
        assertThat(capturedLog.getMetadata()).containsKey("ingestedAt");
    }

    @Test
    void shouldHandleNullTimestamp() {
        // Arrange
        testLogEntry.setTimestamp(null);
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleNullEnvironment() {
        // Arrange
        testLogEntry.setEnvironment(null);
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getEnvironment()).isEqualTo("unknown");
    }

    @Test
    void shouldHandleVeryLongMessage() {
        // Arrange
        String longMessage = "a".repeat(10000);
        testLogEntry.setMessage(longMessage);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("accepted");
    }

    @Test
    void shouldHandleSpecialCharactersInService() {
        // Arrange
        testLogEntry.setService("test-service-with-special-chars-@#$%");
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("accepted");
    }

    @Test
    void shouldPublishToCorrectExchangeAndRoutingKey() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.LOGS_EXCHANGE),
                eq(RabbitMQConfig.LOGS_RAW_ROUTING_KEY),
                any(LogEntryDTO.class)
        );
    }

    @Test
    void shouldGenerateUniqueLogIds() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        LogResponseDTO response1 = logIngestionService.ingestLog(testLogEntry);
        LogResponseDTO response2 = logIngestionService.ingestLog(testLogEntry);
        LogResponseDTO response3 = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response1.getId()).isNotEqualTo(response2.getId());
        assertThat(response2.getId()).isNotEqualTo(response3.getId());
        assertThat(response1.getId()).isNotEqualTo(response3.getId());
    }

    @Test
    void shouldIncludeTimestampInResponse() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));
        Instant before = Instant.now();

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        Instant after = Instant.now();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBetween(before, after);
    }

    @Test
    void shouldHandleComplexMetadata() {
        // Arrange
        Map<String, Object> complexMetadata = new HashMap<>();
        complexMetadata.put("nested", Map.of("key1", "value1", "key2", "value2"));
        complexMetadata.put("array", java.util.Arrays.asList("item1", "item2", "item3"));
        complexMetadata.put("number", 12345);
        complexMetadata.put("boolean", true);
        testLogEntry.setMetadata(complexMetadata);
        
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("accepted");
    }
}

// Made with Bob