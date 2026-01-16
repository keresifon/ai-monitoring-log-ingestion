package com.ibm.aimonitoring.ingestion.controller;

import com.ibm.aimonitoring.ingestion.dto.LogEntryDTO;
import com.ibm.aimonitoring.ingestion.dto.LogResponseDTO;
import com.ibm.aimonitoring.ingestion.service.LogIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for log ingestion operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Log Ingestion", description = "APIs for ingesting log entries")
public class LogController {

    private final LogIngestionService logIngestionService;

    /**
     * Ingest a log entry
     *
     * @param logEntry the log entry to ingest
     * @return response with log ID and status
     */
    @PostMapping
    @Operation(summary = "Ingest a log entry", description = "Accepts a log entry and queues it for processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Log entry accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid log entry"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LogResponseDTO> ingestLog(@Valid @RequestBody LogEntryDTO logEntry) {
        log.debug("Received log entry for service: {}", logEntry.getService());
        
        LogResponseDTO response = logIngestionService.ingestLog(logEntry);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "log-ingestion"
        ));
    }
}
