package com.shao.config;

import com.shao.mq.SettlementExecuteMsg;
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
 * @date: 2022/3/28 11:14
 */
@Configuration
public class RabbitConfig {
    @Value("${rabbitmq.settlement.exchange}")
    private String settlementExchange;

    @Value("${rabbitmq.settlement.queue}")
    private String settlementQueue;

    @Value("${rabbitmq.settlement.key}")
    private String settlementKey;

    @Bean
    @Qualifier("settlementExchange")
    public Exchange settlementExchange() {
        return new DirectExchange(settlementExchange, true, false);
    }

    @Bean
    @Qualifier("settlementQueue")
    public Queue settlementQueue() {
        return new Queue(settlementQueue, true, false, false);
    }

    @Bean
    public Binding settlementBinding() {
        return new Binding(settlementQueue, Binding.DestinationType.QUEUE, settlementExchange, settlementKey, null);
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                         SettlementExecuteMsg settlementExecuteMsg) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setExposeListenerChannel(true);
        container.setConnectionFactory(connectionFactory);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueueNames("queue.settlement");
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(10);
        container.setMessageListener(settlementExecuteMsg);
        return container;
    }
}
