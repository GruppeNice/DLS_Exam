package com.dlsexam.catalogservice.config;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange catalogEventsExchange(@Value("${app.messaging.catalog-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    TopicExchange reviewEventsExchange(@Value("${app.messaging.review-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue reviewEventsQueue() {
        return QueueBuilder.durable("catalog-service.review-events").build();
    }

    @Bean
    Binding contentRatedBinding(Queue reviewEventsQueue, TopicExchange reviewEventsExchange) {
        return BindingBuilder.bind(reviewEventsQueue).to(reviewEventsExchange).with("content.rated");
    }

    @Bean
    Binding contentReviewedBinding(Queue reviewEventsQueue, TopicExchange reviewEventsExchange) {
        return BindingBuilder.bind(reviewEventsQueue).to(reviewEventsExchange).with("content.reviewed");
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
