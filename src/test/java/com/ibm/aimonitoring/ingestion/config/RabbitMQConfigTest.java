package com.ibm.aimonitoring.ingestion.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RabbitMQConfig
 */
@ExtendWith(MockitoExtension.class)
class RabbitMQConfigTest {

    @InjectMocks
    private RabbitMQConfig rabbitMQConfig;

    @Mock
    private ConnectionFactory connectionFactory;

    @Test
    void shouldCreateLogsExchange() {
        // Act
        TopicExchange exchange = rabbitMQConfig.logsExchange();

        // Assert
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.LOGS_EXCHANGE);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void shouldCreateLogsRawQueue() {
        // Act
        Queue queue = rabbitMQConfig.logsRawQueue();

        // Assert
        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.LOGS_RAW_QUEUE);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsKey("x-dead-letter-exchange")
                .containsKey("x-dead-letter-routing-key")
                .containsEntry("x-dead-letter-routing-key", RabbitMQConfig.LOGS_DLQ);
    }

    @Test
    void shouldCreateDeadLetterQueue() {
        // Act
        Queue queue = rabbitMQConfig.logsDeadLetterQueue();

        // Assert
        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.LOGS_DLQ);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void shouldCreateBindingBetweenExchangeAndQueue() {
        // Arrange
        Queue queue = rabbitMQConfig.logsRawQueue();
        TopicExchange exchange = rabbitMQConfig.logsExchange();

        // Act
        Binding binding = rabbitMQConfig.logsRawBinding(queue, exchange);

        // Assert
        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.LOGS_RAW_QUEUE);
        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.LOGS_EXCHANGE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.LOGS_RAW_ROUTING_KEY);
    }

    @Test
    void shouldCreateJsonMessageConverter() {
        // Act
        MessageConverter converter = rabbitMQConfig.jsonMessageConverter();

        // Assert
        assertThat(converter).isNotNull();
        assertThat(converter.getClass().getSimpleName()).contains("Jackson2Json");
    }

    @Test
    void shouldCreateRabbitTemplateWithJsonConverter() {
        // Act
        RabbitTemplate template = rabbitMQConfig.rabbitTemplate(connectionFactory);

        // Assert
        assertThat(template).isNotNull();
        assertThat(template.getMessageConverter()).isNotNull();
        assertThat(template.getMessageConverter().getClass().getSimpleName()).contains("Jackson2Json");
    }

    @Test
    void shouldCreateRabbitAdmin() {
        // Act
        RabbitAdmin admin = rabbitMQConfig.rabbitAdmin(connectionFactory);

        // Assert
        assertThat(admin).isNotNull();
    }

    @Test
    void shouldHaveCorrectConstantValues() {
        // Assert
        assertThat(RabbitMQConfig.LOGS_EXCHANGE).isEqualTo("logs.exchange");
        assertThat(RabbitMQConfig.LOGS_RAW_QUEUE).isEqualTo("logs.raw");
        assertThat(RabbitMQConfig.LOGS_RAW_ROUTING_KEY).isEqualTo("logs.raw");
        assertThat(RabbitMQConfig.LOGS_DLQ).isEqualTo("logs.dlq");
    }
}

// Made with Bob