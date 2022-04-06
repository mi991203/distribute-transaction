package com.shao.sender;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/4/1 9:58
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shao.po.TransactionMsgPO;
import com.shao.service.TransactionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionMsgSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TransactionMsgService transactionMsgService;

    public void send(String exchange, String routingKey, Object payload) {
        final ObjectMapper objectMapper = new ObjectMapper();
        String messageId = null;
        try {
            final String payloadStr = objectMapper.writeValueAsString(payload);
            // 先把消息落到数据库
            final TransactionMsgPO transactionMsgPO = transactionMsgService.messageSendReady(exchange, routingKey, payloadStr);
            // 设置消息属性
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            final Message message = new Message(payloadStr.getBytes(), messageProperties);
            // 设置消息ID
            messageId = transactionMsgPO.getId();
            message.getMessageProperties().setMessageId(messageId);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, new CorrelationData(messageId));
            log.info("发送消息,消息ID:{}", messageId);
        } catch (Exception e) {
            log.error("发送消息失败，messageUnique={}", messageId);
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }

}
