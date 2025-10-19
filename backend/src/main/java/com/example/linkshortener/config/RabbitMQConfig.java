package com.example.linkshortener.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Queue names
    public static final String URL_SHORTENING_QUEUE = "url-shortening-queue";
    public static final String CLICK_TRACKING_QUEUE = "click-tracking-queue";
    public static final String CACHE_SYNC_QUEUE = "cache-sync-queue";

    // Exchange names
    public static final String URL_SHORTENING_EXCHANGE = "url-shortening-exchange";
    public static final String CLICK_TRACKING_EXCHANGE = "click-tracking-exchange";
    public static final String CACHE_SYNC_EXCHANGE = "cache-sync-exchange";

    // Routing keys
    public static final String URL_SHORTENING_ROUTING_KEY = "url.shorten";
    public static final String CLICK_TRACKING_ROUTING_KEY = "url.click";
    public static final String CACHE_SYNC_ROUTING_KEY = "cache.sync";

    // Dead Letter Exchange
    public static final String DEAD_LETTER_EXCHANGE = "dead-letter-exchange";
    public static final String DEAD_LETTER_QUEUE = "dead-letter-queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead-letter";

    @Bean
    public Queue urlShorteningQueue() {
        return QueueBuilder.durable(URL_SHORTENING_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue clickTrackingQueue() {
        return QueueBuilder.durable(CLICK_TRACKING_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue cacheSyncQueue() {
        return QueueBuilder.durable(CACHE_SYNC_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public DirectExchange urlShorteningExchange() {
        return new DirectExchange(URL_SHORTENING_EXCHANGE);
    }

    @Bean
    public DirectExchange clickTrackingExchange() {
        return new DirectExchange(CLICK_TRACKING_EXCHANGE);
    }

    @Bean
    public DirectExchange cacheSyncExchange() {
        return new DirectExchange(CACHE_SYNC_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding urlShorteningBinding() {
        return BindingBuilder
                .bind(urlShorteningQueue())
                .to(urlShorteningExchange())
                .with(URL_SHORTENING_ROUTING_KEY);
    }

    @Bean
    public Binding clickTrackingBinding() {
        return BindingBuilder
                .bind(clickTrackingQueue())
                .to(clickTrackingExchange())
                .with(CLICK_TRACKING_ROUTING_KEY);
    }

    @Bean
    public Binding cacheSyncBinding() {
        return BindingBuilder
                .bind(cacheSyncQueue())
                .to(cacheSyncExchange())
                .with(CACHE_SYNC_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}