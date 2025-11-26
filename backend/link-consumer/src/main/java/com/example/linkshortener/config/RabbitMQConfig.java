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

    // Must match the producer configuration
    public static final String EXCHANGE_NAME = "link_events_exchange";
    public static final String LINK_CREATION_QUEUE = "link_creation_queue";
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
        return new Jackson2JsonMessageConverter();
    }
}