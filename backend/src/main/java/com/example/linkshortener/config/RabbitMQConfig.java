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
    public static final String EXCHANGE_NAME = "link_events_exchange";

    public static final String CLICK_ANALYTICS_QUEUE = "click_analytics_queue";
    public static final String QR_CREATION_QUEUE = "qr_creation_queue";

    public static final String CLICK_ROUTING_KEY = "click";
    public static final String QR_ROUTING_KEY = "qr";

    @Bean
    public DirectExchange linkEventsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue clickAnalyticsQueue() {
        return new Queue(CLICK_ANALYTICS_QUEUE, true); // Durable
    }

    @Bean
    public Queue qrCreationQueue() {
        return new Queue(QR_CREATION_QUEUE, true); // Durable
    }

    @Bean
    public Binding clickBinding(Queue clickAnalyticsQueue, DirectExchange linkEventsExchange) {
        return BindingBuilder.bind(clickAnalyticsQueue)
                .to(linkEventsExchange)
                .with(CLICK_ROUTING_KEY);
    }

    @Bean
    public Binding qrBinding(Queue qrCreationQueue, DirectExchange linkEventsExchange) {
        return BindingBuilder.bind(qrCreationQueue)
                .to(linkEventsExchange)
                .with(QR_ROUTING_KEY);
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
