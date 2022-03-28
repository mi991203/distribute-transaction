package com.shao.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 10:13
 */
@Configuration
public class RabbitConfig {
    @Value("${rabbitmq.deliveryman.exchange}")
    private String deliveryExchange;

    @Value("${rabbitmq.deliveryman.queue}")
    private String deliveryQueue;

    @Value("${rabbitmq.deliveryman.key}")
    private String deliveryKey;

    @Bean
    @Qualifier("deliveryExchange")
    public Exchange deliveryExchange() {
        return new DirectExchange(deliveryExchange, true, false, null);
    }

    @Bean
    @Qualifier("deliveryQueue")
    public Queue deliveryQueue() {
        return new Queue(deliveryQueue, true, false, false);
    }

    @Bean
    @Qualifier("deliveryBinding")
    public Binding deliveryBinding() {
        return new Binding(deliveryQueue, Binding.DestinationType.QUEUE, deliveryExchange, deliveryKey, null);
    }
}
