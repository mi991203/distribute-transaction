package com.shao.config;

import com.shao.listener.DlxListener;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/4/2 16:41
 */
@Configuration
@ConditionalOnProperty("rabbitmq.dlxEnabled")
public class DlxConfig {
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange("exchange.dlx");
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue("queue.dlx", true, false, false);
    }

    @Bean
    public Binding dlxBinding() {
        return new Binding("queue.dlx", Binding.DestinationType.QUEUE, "exchange.dlx", "#", null);
    }

    @Bean
    public SimpleMessageListenerContainer deadLetterListenerContainer(ConnectionFactory connectionFactory, DlxListener dlxListener) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(dlxQueue());
        container.setExposeListenerChannel(true);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(dlxListener);
        container.setPrefetchCount(50);
        return container;
    }
}
