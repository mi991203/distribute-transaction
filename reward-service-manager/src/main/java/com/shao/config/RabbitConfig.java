package com.shao.config;

import org.springframework.amqp.core.*;
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
}
