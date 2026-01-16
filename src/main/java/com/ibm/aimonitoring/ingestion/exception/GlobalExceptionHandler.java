package com.ibm.aimonitoring.ingestion.exception;

import com.ibm.aimonitoring.ingestion.service.LogIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String STATUS_KEY = "status";
    private static final String ERROR_KEY = "error";

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP_KEY, Instant.now().toString());
        response.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR_KEY, "Validation Failed");
        response.put("errors", errors);

        log.warn("Validation error: {}", errors);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle log ingestion exceptions
     */
    @ExceptionHandler(LogIngestionService.LogIngestionException.class)
    public ResponseEntity<Map<String, Object>> handleLogIngestionException(
            LogIngestionService.LogIngestionException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP_KEY, Instant.now().toString());
        response.put(STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put(ERROR_KEY, "Log Ingestion Failed");
        response.put("message", ex.getMessage());

        log.error("Log ingestion error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP_KEY, Instant.now().toString());
        response.put(STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put(ERROR_KEY, "Internal Server Error");
        response.put("message", "An unexpected error occurred");

        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

// Made with Bob
