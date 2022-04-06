package com.shao.config;

import com.shao.mq.DeliverymanExecuteMsg;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                         DeliverymanExecuteMsg deliverymanExecuteMsg) {
        final SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueueNames("queue.deliveryman");
        simpleMessageListenerContainer.setExposeListenerChannel(true);
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleMessageListenerContainer.setMessageListener(deliverymanExecuteMsg);
        simpleMessageListenerContainer.setConcurrentConsumers(1);
        simpleMessageListenerContainer.setMaxConcurrentConsumers(10);
        return simpleMessageListenerContainer;
    }
}
