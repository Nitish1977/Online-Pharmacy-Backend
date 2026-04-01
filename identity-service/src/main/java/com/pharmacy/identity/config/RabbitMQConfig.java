package com.pharmacy.identity.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "pharmacy.exchange";
    public static final String QUEUE_STOCK_LOW = "stock.low.queue";
    public static final String ROUTING_KEY_STOCK_LOW = "stock.low";
    public static final String ROUTING_KEY_NOTIFICATION = "notification.email";

    @Bean
    public Queue stockLowQueue() {
        return new Queue(QUEUE_STOCK_LOW, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingStockLow(Queue stockLowQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(stockLowQueue).to(topicExchange).with(ROUTING_KEY_STOCK_LOW);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

