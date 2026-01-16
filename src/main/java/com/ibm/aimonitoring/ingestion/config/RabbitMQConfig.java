package com.ibm.aimonitoring.ingestion.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for log ingestion
 */
@Configuration
public class RabbitMQConfig {

    public static final String LOGS_EXCHANGE = "logs.exchange";
    public static final String LOGS_RAW_QUEUE = "logs.raw";
    public static final String LOGS_RAW_ROUTING_KEY = "logs.raw";
    public static final String LOGS_DLQ = "logs.dlq";

    /**
     * Topic exchange for log routing
     */
    @Bean
    public TopicExchange logsExchange() {
        return new TopicExchange(LOGS_EXCHANGE, true, false);
    }

    /**
     * Queue for raw ingested logs
     */
    @Bean
    public Queue logsRawQueue() {
        return QueueBuilder.durable(LOGS_RAW_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", LOGS_DLQ)
                .build();
    }

    /**
     * Dead letter queue for failed messages
     */
    @Bean
    public Queue logsDeadLetterQueue() {
        return QueueBuilder.durable(LOGS_DLQ).build();
    }

    /**
     * Binding between exchange and raw logs queue
     */
    @Bean
    public Binding logsRawBinding(Queue logsRawQueue, TopicExchange logsExchange) {
        return BindingBuilder
                .bind(logsRawQueue)
                .to(logsExchange)
                .with(LOGS_RAW_ROUTING_KEY);
    }

    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * RabbitAdmin for automatic queue/exchange declaration
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}

// Made with Bob
