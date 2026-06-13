package com.engagementservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String NOTIFICATION_QUEUE = "notification-queue";
    public static final String DOMAIN_EVENTS_QUEUE = "engagement-service.domain-events";

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    Queue domainEventsQueue() {
        return QueueBuilder.durable(DOMAIN_EVENTS_QUEUE).build();
    }

    @Bean
    TopicExchange billingEventsExchange(@Value("${app.messaging.billing-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    TopicExchange streamingEventsExchange(@Value("${app.messaging.streaming-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    TopicExchange catalogEventsExchange(@Value("${app.messaging.catalog-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Binding subscriptionActivatedBinding(Queue domainEventsQueue, TopicExchange billingEventsExchange) {
        return BindingBuilder.bind(domainEventsQueue).to(billingEventsExchange).with("subscription.activated");
    }

    @Bean
    Binding playbackStoppedBinding(Queue domainEventsQueue, TopicExchange streamingEventsExchange) {
        return BindingBuilder.bind(domainEventsQueue).to(streamingEventsExchange).with("playback.stopped");
    }

    @Bean
    Binding contentCreatedBinding(Queue domainEventsQueue, TopicExchange catalogEventsExchange) {
        return BindingBuilder.bind(domainEventsQueue).to(catalogEventsExchange).with("content.created");
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
