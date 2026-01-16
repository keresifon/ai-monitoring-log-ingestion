package com.ibm.aimonitoring.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for log ingestion response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponseDTO {

    private String id;
    
    private String status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    private String message;
}

// Made with Bob
