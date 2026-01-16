package com.ibm.aimonitoring.ingestion.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LogLevel enum
 */
class LogLevelTest {

    @Test
    void shouldHaveAllLogLevels() {
        // Act
        LogLevel[] levels = LogLevel.values();

        // Assert
        assertThat(levels)
                .hasSize(5)
                .containsExactly(
                        LogLevel.ERROR,
                        LogLevel.WARN,
                        LogLevel.INFO,
                        LogLevel.DEBUG,
                        LogLevel.TRACE
                );
    }

    @ParameterizedTest
    @CsvSource({
            "ERROR,ERROR",
            "WARN,WARN",
            "INFO,INFO",
            "DEBUG,DEBUG",
            "TRACE,TRACE"
    })
    void shouldConvertStringToLogLevel(String input, LogLevel expected) {
        // Act & Assert
        assertThat(LogLevel.valueOf(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "ERROR,ERROR",
            "WARN,WARN",
            "INFO,INFO",
            "DEBUG,DEBUG",
            "TRACE,TRACE"
    })
    void shouldHaveCorrectEnumNames(LogLevel level, String expectedName) {
        // Assert
        assertThat(level.name()).isEqualTo(expectedName);
    }

    @ParameterizedTest
    @CsvSource({
            "ERROR,0",
            "WARN,1",
            "INFO,2",
            "DEBUG,3",
            "TRACE,4"
    })
    void shouldHaveCorrectOrdinalValues(LogLevel level, int expectedOrdinal) {
        // Assert
        assertThat(level.ordinal()).isEqualTo(expectedOrdinal);
    }

    @ParameterizedTest
    @CsvSource({
            "ERROR,WARN",
            "WARN,INFO",
            "INFO,DEBUG",
            "DEBUG,TRACE"
    })
    void shouldSupportEnumComparison(LogLevel lower, LogLevel higher) {
        // Assert - lower severity has lower ordinal
        assertThat(lower).isLessThan(higher);
    }

    @Test
    void shouldBeUsableInSwitchStatement() {
        // Arrange
        LogLevel level = LogLevel.ERROR;
        String result;

        // Act
        switch (level) {
            case ERROR:
                result = "Error level";
                break;
            case WARN:
                result = "Warning level";
                break;
            case INFO:
                result = "Info level";
                break;
            case DEBUG:
                result = "Debug level";
                break;
            case TRACE:
                result = "Trace level";
                break;
            default:
                result = "Unknown";
        }

        // Assert
        assertThat(result).isEqualTo("Error level");
    }
}

// Made with Bob