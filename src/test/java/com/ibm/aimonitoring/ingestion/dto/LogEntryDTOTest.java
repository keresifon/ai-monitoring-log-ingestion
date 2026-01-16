package com.ibm.aimonitoring.ingestion.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LogEntryDTO validation
 */
class LogEntryDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithAllRequiredFields() {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .timestamp(Instant.now())
                .level("ERROR")
                .message("Test error message")
                .service("test-service")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenLevelIsNull() {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .message("Test message")
                .service("test-service")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Log level is required");
    }

    @Test
    void shouldFailValidationWhenLevelIsInvalid() {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INVALID")
                .message("Test message")
                .service("test-service")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Log level must be one of");
    }

    @Test
    void shouldFailValidationWhenMessageIsBlank() {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("")
                .service("test-service")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Message is required");
    }

    @Test
    void shouldFailValidationWhenMessageExceedsMaxLength() {
        // Arrange
        String longMessage = "a".repeat(10001);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message(longMessage)
                .service("test-service")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 10000 characters");
    }

    @Test
    void shouldFailValidationWhenServiceIsBlank() {
        // Arrange
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test message")
                .service("")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Service name is required");
    }

    @Test
    void shouldFailValidationWhenServiceExceedsMaxLength() {
        // Arrange
        String longService = "a".repeat(101);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test message")
                .service(longService)
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 100 characters");
    }

    @Test
    void shouldPassValidationWithAllValidLogLevels() {
        // Arrange
        String[] validLevels = {"ERROR", "WARN", "INFO", "DEBUG", "TRACE"};

        for (String level : validLevels) {
            LogEntryDTO logEntry = LogEntryDTO.builder()
                    .level(level)
                    .message("Test message")
                    .service("test-service")
                    .build();

            // Act
            Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldPassValidationWithOptionalFields() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", "user123");
        metadata.put("requestId", "req456");

        LogEntryDTO logEntry = LogEntryDTO.builder()
                .timestamp(Instant.now())
                .level("INFO")
                .message("Test message")
                .service("test-service")
                .host("localhost")
                .environment("production")
                .metadata(metadata)
                .traceId("trace-123")
                .spanId("span-456")
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenHostExceedsMaxLength() {
        // Arrange
        String longHost = "a".repeat(256);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test message")
                .service("test-service")
                .host(longHost)
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 255 characters");
    }

    @Test
    void shouldFailValidationWhenEnvironmentExceedsMaxLength() {
        // Arrange
        String longEnv = "a".repeat(101);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test message")
                .service("test-service")
                .environment(longEnv)
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 100 characters");
    }

    @Test
    void shouldFailValidationWhenTraceIdExceedsMaxLength() {
        // Arrange
        String longTraceId = "a".repeat(101);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test message")
                .service("test-service")
                .traceId(longTraceId)
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 100 characters");
    }

    @Test
    void shouldFailValidationWhenSpanIdExceedsMaxLength() {
        // Arrange
        String longSpanId = "a".repeat(101);
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .level("INFO")
                .message("Test message")
                .service("test-service")
                .spanId(longSpanId)
                .build();

        // Act
        Set<ConstraintViolation<LogEntryDTO>> violations = validator.validate(logEntry);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 100 characters");
    }

    @Test
    void shouldBuildLogEntryWithBuilder() {
        // Arrange & Act
        LogEntryDTO logEntry = LogEntryDTO.builder()
                .timestamp(Instant.now())
                .level("ERROR")
                .message("Test error")
                .service("api-service")
                .host("server1")
                .environment("prod")
                .traceId("trace-1")
                .spanId("span-1")
                .build();

        // Assert
        assertThat(logEntry.getLevel()).isEqualTo("ERROR");
        assertThat(logEntry.getMessage()).isEqualTo("Test error");
        assertThat(logEntry.getService()).isEqualTo("api-service");
        assertThat(logEntry.getHost()).isEqualTo("server1");
        assertThat(logEntry.getEnvironment()).isEqualTo("prod");
        assertThat(logEntry.getTraceId()).isEqualTo("trace-1");
        assertThat(logEntry.getSpanId()).isEqualTo("span-1");
    }
}

// Made with Bob