package com.example.linkshortener.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Exchange ---
    public static final String EXCHANGE_NAME = "link_events_exchange";

    // --- Queue ---
    public static final String LINK_CREATION_QUEUE = "link_creation_queue";

    // --- Routing Key ---
    public static final String LINK_CREATION_ROUTING_KEY = "link.create";

    @Bean
    public DirectExchange linkEventsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue linkCreationQueue() {
        return new Queue(LINK_CREATION_QUEUE, true); // Durable
    }

    @Bean
    public Binding linkCreationBinding(Queue linkCreationQueue, DirectExchange linkEventsExchange) {
        return BindingBuilder.bind(linkCreationQueue)
                .to(linkEventsExchange)
                .with(LINK_CREATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // This allows RabbitMQ to send and receive plain Java objects as JSON
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}