package com.ibm.aimonitoring.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for incoming log entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    @NotNull(message = "Log level is required")
    @Pattern(regexp = "ERROR|WARN|INFO|DEBUG|TRACE", message = "Log level must be one of: ERROR, WARN, INFO, DEBUG, TRACE")
    private String level;

    @NotBlank(message = "Message is required")
    @Size(max = 10000, message = "Message must not exceed 10000 characters")
    private String message;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    private String service;

    @Size(max = 255, message = "Host name must not exceed 255 characters")
    private String host;

    @Size(max = 100, message = "Environment must not exceed 100 characters")
    private String environment;

    private Map<String, Object> metadata;

    @Size(max = 100, message = "Trace ID must not exceed 100 characters")
    private String traceId;

    @Size(max = 100, message = "Span ID must not exceed 100 characters")
    private String spanId;
}

// Made with Bob
