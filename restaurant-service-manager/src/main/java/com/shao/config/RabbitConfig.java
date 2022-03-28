package com.shao.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${rabbitmq.restaurant.exchange}")
    private String restaurantExchange;

    @Value("${rabbitmq.restaurant.key}")
    private String restaurantKey;

    @Value("${rabbitmq.restaurant.queue}")
    private String restaurantQueue;

    @Bean
    @Qualifier("restaurantQueue")
    public Queue restaurantQueue() {
        return new Queue(restaurantQueue, true, false, false, null);
    }

    @Bean
    @Qualifier("restaurantBinding")
    public Binding restaurantBinding() {
        return new Binding(restaurantQueue, Binding.DestinationType.QUEUE, restaurantExchange, restaurantKey, null);
    }

}
