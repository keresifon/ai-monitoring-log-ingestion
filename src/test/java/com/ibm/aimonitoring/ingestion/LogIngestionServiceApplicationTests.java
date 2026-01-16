package com.ibm.aimonitoring.ingestion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Integration test for Spring Boot application context.
 * Uses mocked RabbitMQ components to avoid external dependencies.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogIngestionServiceApplicationTests {

    @MockBean
    private RabbitTemplate rabbitTemplate;
    
    @MockBean
    private RabbitAdmin rabbitAdmin;

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // with mocked external dependencies
    }
}

// Made with Bob
