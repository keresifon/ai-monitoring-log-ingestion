package com.ibm.aimonitoring.ingestion.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.mockito.Mockito.*;

/**
 * Unit tests for RabbitMQInitializer
 */
@ExtendWith(MockitoExtension.class)
class RabbitMQInitializerTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @InjectMocks
    private RabbitMQInitializer rabbitMQInitializer;

    @Test
    void shouldInitializeRabbitMQSuccessfully() {
        // Arrange
        doNothing().when(rabbitAdmin).initialize();

        // Act
        rabbitMQInitializer.initializeRabbitMQ();

        // Assert
        verify(rabbitAdmin, times(1)).initialize();
    }

    @Test
    void shouldHandleInitializationFailure() {
        // Arrange
        doThrow(new RuntimeException("Connection failed")).when(rabbitAdmin).initialize();

        // Act & Assert
        try {
            rabbitMQInitializer.initializeRabbitMQ();
        } catch (RabbitMQInitializer.RabbitMQInitializationException e) {
            // Expected exception
            verify(rabbitAdmin, times(1)).initialize();
        }
    }
}

// Made with Bob