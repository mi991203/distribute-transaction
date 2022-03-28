package com.shao.config;

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {
    @Value("${rabbitmq.restaurant.exchange}")
    private String restaurantExchange;

    @Value("${rabbitmq.deliveryman.exchange}")
    private String deliverymanExchange;

    @Value("${rabbitmq.settlement.exchange}")
    private String settlementExchange;

    @Value("${rabbitmq.reward.exchange}")
    private String rewardExchange;

    @Value("${rabbitmq.order.queue}")
    private String orderQueue;

    @Value("${rabbitmq.order.key}")
    private String orderKey;



    @Bean
    @Qualifier("restaurantExchange")
    public Exchange restaurantExchange() {
        return new DirectExchange(restaurantExchange, true, false, null);
    }

    @Bean
    @Qualifier("deliverymanExchange")
    public Exchange deliverymanExchange() {
        return new DirectExchange(deliverymanExchange, true, false, null);
    }

    @Bean
    @Qualifier("settlementExchange")
    public Exchange settlementExchange() {
        return new DirectExchange(settlementExchange, true, false, null);
    }

    @Bean
    @Qualifier("rewardExchange")
    public Exchange rewardExchange() {
        return new DirectExchange(rewardExchange, true, false, null);
    }

    @Bean
    @Qualifier("orderQueue")
    public Queue orderQueue() {
        return new Queue(orderQueue, true, false, false);
    }

    @Bean
    public Binding restaurantBinding() {
        return new Binding(orderQueue, Binding.DestinationType.QUEUE, restaurantExchange, orderKey, null);
    }

    @Bean
    public Binding deliverymanBinding() {
        return new Binding(orderQueue, Binding.DestinationType.QUEUE, deliverymanExchange, orderKey, null);
    }

    @Bean
    public Binding settlementBinding() {
        return new Binding(orderQueue, Binding.DestinationType.QUEUE, settlementExchange, orderKey, null);
    }

    @Bean
    public Binding rewardBinding() {
        return new Binding(orderQueue, Binding.DestinationType.QUEUE, rewardExchange, orderKey, null);
    }

}
