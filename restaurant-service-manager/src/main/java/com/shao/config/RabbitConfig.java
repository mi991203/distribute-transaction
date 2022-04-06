package com.shao.config;

import com.shao.mq.RestaurantExecuteMsg;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                         RestaurantExecuteMsg restaurantExecuteMsg) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setQueueNames("queue.restaurant");
        container.setExposeListenerChannel(true);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(restaurantExecuteMsg);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(10);
        container.setConnectionFactory(connectionFactory);
        return container;
    }

}
