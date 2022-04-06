package com.shao.listener;

import com.rabbitmq.client.Channel;
import com.shao.service.TransactionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/4/2 16:45
 */
@Component
@ConditionalOnProperty("rabbitmq.dlxEnabled")
@Slf4j
public class DlxListener implements ChannelAwareMessageListener {
    @Autowired
    private TransactionMsgService transactionMsgService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        final String messageBody = new String(message.getBody());
        log.error("dead letter message: {} | tag: {}", messageBody, message.getMessageProperties().getDeliveryTag());
        // do more alarm...

        transactionMsgService.messageDead(message.getMessageProperties().getMessageId(), message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), message.getMessageProperties().getConsumerQueue(),
                new String(message.getBody()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
