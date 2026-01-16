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
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogIngestionService
 */
@ExtendWith(MockitoExtension.class)
class LogIngestionServiceTest {

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
    void shouldIngestLogSuccessfully() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        LogResponseDTO response = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("accepted");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getMessage()).contains("accepted");

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.LOGS_EXCHANGE),
                eq(RabbitMQConfig.LOGS_RAW_ROUTING_KEY),
                any(LogEntryDTO.class)
        );
    }

    @Test
    void shouldEnrichLogWithMetadata() {
        // Arrange
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getMetadata()).isNotNull();
        assertThat(capturedLog.getMetadata()).containsKey("logId");
        assertThat(capturedLog.getMetadata()).containsKey("ingestedAt");
    }

    @Test
    void shouldAddTimestampIfMissing() {
        // Arrange
        testLogEntry.setTimestamp(null);
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getTimestamp()).isNotNull();
    }

    @Test
    void shouldSetDefaultEnvironmentIfMissing() {
        // Arrange
        testLogEntry.setEnvironment(null);
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getEnvironment()).isEqualTo("unknown");
    }

    @Test
    void shouldPublishToCorrectQueue() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(LogEntryDTO.class));

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
    void shouldHandleRabbitMQFailure() {
        // Arrange
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(
                        anyString(), anyString(), any(LogEntryDTO.class));

        // Act & Assert
        assertThatThrownBy(() -> logIngestionService.ingestLog(testLogEntry))
                .isInstanceOf(LogIngestionService.LogIngestionException.class)
                .hasMessageContaining("Failed to ingest log entry");
    }

    @Test
    void shouldGenerateUniqueLogId() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(LogEntryDTO.class));

        // Act
        LogResponseDTO response1 = logIngestionService.ingestLog(testLogEntry);
        LogResponseDTO response2 = logIngestionService.ingestLog(testLogEntry);

        // Assert
        assertThat(response1.getId()).isNotEqualTo(response2.getId());
    }

    @Test
    void shouldInitializeMetadataIfNull() {
        // Arrange
        testLogEntry.setMetadata(null);
        ArgumentCaptor<LogEntryDTO> logCaptor = ArgumentCaptor.forClass(LogEntryDTO.class);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), logCaptor.capture());

        // Act
        logIngestionService.ingestLog(testLogEntry);

        // Assert
        LogEntryDTO capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getMetadata()).isNotNull();
        assertThat(capturedLog.getMetadata()).isNotEmpty();
    }
}

// Made with Bob
