package com.ibm.aimonitoring.ingestion.service;

import com.ibm.aimonitoring.ingestion.config.RabbitMQConfig;
import com.ibm.aimonitoring.ingestion.dto.LogEntryDTO;
import com.ibm.aimonitoring.ingestion.dto.LogResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for handling log ingestion business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogIngestionService {

    private static final String UNKNOWN_ENVIRONMENT = "unknown";

    private final RabbitTemplate rabbitTemplate;

    /**
     * Ingest a log entry and publish to RabbitMQ
     *
     * @param logEntry the log entry to ingest
     * @return response with log ID and status
     */
    public LogResponseDTO ingestLog(LogEntryDTO logEntry) {
        try {
            // Generate unique log ID
            String logId = UUID.randomUUID().toString();
            
            // Enrich log entry with metadata
            LogEntryDTO enrichedLog = enrichLogEntry(logEntry, logId);
            
            // Publish to RabbitMQ
            publishToQueue(enrichedLog);
            
            log.debug("Successfully ingested log with ID: {}", logId);
            
            return LogResponseDTO.builder()
                    .id(logId)
                    .status("accepted")
                    .timestamp(Instant.now())
                    .message("Log entry accepted for processing")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error ingesting log: {}", e.getMessage(), e);
            throw new LogIngestionException("Failed to ingest log entry", e);
        }
    }

    /**
     * Enrich log entry with additional metadata
     */
    private LogEntryDTO enrichLogEntry(LogEntryDTO logEntry, String logId) {
        // Set timestamp if not provided
        if (logEntry.getTimestamp() == null) {
            logEntry.setTimestamp(Instant.now());
        }
        
        // Set default environment if not provided
        if (logEntry.getEnvironment() == null) {
            logEntry.setEnvironment(UNKNOWN_ENVIRONMENT);
        }
        
        // Add log ID to metadata
        if (logEntry.getMetadata() == null) {
            logEntry.setMetadata(new java.util.HashMap<>());
        }
        logEntry.getMetadata().put("logId", logId);
        logEntry.getMetadata().put("ingestedAt", Instant.now().toString());
        
        return logEntry;
    }

    /**
     * Publish log entry to RabbitMQ queue
     */
    private void publishToQueue(LogEntryDTO logEntry) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.LOGS_EXCHANGE,
                    RabbitMQConfig.LOGS_RAW_ROUTING_KEY,
                    logEntry
            );
            log.debug("Published log to queue: {}", RabbitMQConfig.LOGS_RAW_QUEUE);
        } catch (Exception e) {
            log.error("Failed to publish log to RabbitMQ: {}", e.getMessage(), e);
            throw new LogIngestionException("Failed to publish log to message queue", e);
        }
    }

    /**
     * Custom exception for log ingestion errors
     */
    public static class LogIngestionException extends RuntimeException {
        public LogIngestionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

// Made with Bob
