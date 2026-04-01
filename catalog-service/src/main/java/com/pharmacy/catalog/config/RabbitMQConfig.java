package com.pharmacy.catalog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "pharmacy.exchange";
    public static final String QUEUE_STOCK = "stock.queue";
    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";
    public static final String ROUTING_KEY_STOCK_LOW = "stock.low";

    @Bean
    public Queue stockQueue() {
        return new Queue(QUEUE_STOCK, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingStock(Queue stockQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(stockQueue).to(topicExchange).with(ROUTING_KEY_ORDER_PLACED);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
