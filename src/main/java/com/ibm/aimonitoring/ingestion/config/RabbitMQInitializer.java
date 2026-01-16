package com.ibm.aimonitoring.ingestion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializes RabbitMQ queues and exchanges on application startup
 */
@Slf4j
@Component
public class RabbitMQInitializer {

    private final RabbitAdmin rabbitAdmin;

    public RabbitMQInitializer(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    /**
     * Custom exception for RabbitMQ initialization failures
     */
    public static class RabbitMQInitializationException extends RuntimeException {
        public RabbitMQInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRabbitMQ() {
        log.info("Initializing RabbitMQ infrastructure...");
        
        try {
            // This will trigger the declaration of all @Bean Queue, Exchange, and Binding
            rabbitAdmin.initialize();
            log.info("✓ RabbitMQ queues and exchanges declared successfully");
            log.info("  - Exchange: {}", RabbitMQConfig.LOGS_EXCHANGE);
            log.info("  - Queue: {}", RabbitMQConfig.LOGS_RAW_QUEUE);
            log.info("  - DLQ: {}", RabbitMQConfig.LOGS_DLQ);
        } catch (Exception e) {
            log.error("✗ Failed to initialize RabbitMQ infrastructure", e);
            throw new RabbitMQInitializationException("RabbitMQ initialization failed", e);
        }
    }
}

// Made with Bob