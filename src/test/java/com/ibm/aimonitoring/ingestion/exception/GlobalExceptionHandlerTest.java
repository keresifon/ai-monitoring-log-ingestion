package com.ibm.aimonitoring.ingestion.exception;

import com.ibm.aimonitoring.ingestion.service.LogIngestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void shouldHandleValidationExceptions() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("logEntry", "level", "Log level is required");
        FieldError fieldError2 = new FieldError("logEntry", "message", "Message is required");
        
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
            .containsEntry("status", 400)
            .containsEntry("error", "Validation Failed")
            .containsKey("timestamp")
            .containsKey("errors");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors)
            .containsEntry("level", "Log level is required")
            .containsEntry("message", "Message is required");
    }

    @Test
    void shouldHandleLogIngestionException() {
        // Arrange
        LogIngestionService.LogIngestionException exception = 
            new LogIngestionService.LogIngestionException("Failed to publish to RabbitMQ", new RuntimeException("Connection failed"));

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleLogIngestionException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
            .containsEntry("status", 500)
            .containsEntry("error", "Log Ingestion Failed")
            .containsEntry("message", "Failed to publish to RabbitMQ")
            .containsKey("timestamp");
    }

    @Test
    void shouldHandleGenericException() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
            .containsEntry("status", 500)
            .containsEntry("error", "Internal Server Error")
            .containsEntry("message", "An unexpected error occurred")
            .containsKey("timestamp");
    }

    @Test
    void shouldHandleNullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null value encountered");

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
            .containsEntry("error", "Internal Server Error");
    }

    @Test
    void shouldIncludeTimestampInAllResponses() {
        // Arrange
        Exception exception = new Exception("Test exception");

        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getBody())
            .isNotNull()
            .containsKey("timestamp");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }
}

// Made with Bob