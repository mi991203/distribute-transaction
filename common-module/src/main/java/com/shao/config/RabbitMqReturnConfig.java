package com.shao.config;

import com.shao.service.TransactionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class RabbitMqReturnConfig implements RabbitTemplate.ReturnCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TransactionMsgService transactionMsgService;

    @PostConstruct
    public void init() {
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.error("消息无法被RabbitMq路由，message={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                message, replyCode, replyText, exchange, routingKey);
        transactionMsgService.messageSendReturn(message.getMessageProperties().getMessageId(), exchange, routingKey, new String(message.getBody()));
    }
}
