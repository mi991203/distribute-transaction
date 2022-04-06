package com.shao.config;

import com.shao.mq.RewardExecuteMsg;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 13:45
 */
@Configuration
public class RabbitConfig {
    @Value("${rabbitmq.reward.exchange}")
    private String rewardExchange;

    @Value("${rabbitmq.reward.queue}")
    private String rewardQueue;

    @Value("${rabbitmq.reward.key}")
    private String rewardKey;


    @Bean
    public Exchange rewardExchange() {
        return new DirectExchange(rewardExchange, true, false);
    }

    @Bean
    public Queue rewardQueue() {
        return new Queue(rewardQueue, true, false, false);
    }

    @Bean
    public Binding rewardBinding() {
        return new Binding(rewardQueue, Binding.DestinationType.QUEUE, rewardExchange, rewardKey, null);
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                         RewardExecuteMsg rewardExecuteMsg) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setExposeListenerChannel(true);
        container.setConnectionFactory(connectionFactory);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueueNames("queue.reward");
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(10);
        container.setMessageListener(rewardExecuteMsg);
        return container;
    }
}
