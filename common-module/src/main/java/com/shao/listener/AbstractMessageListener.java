package com.shao.listener;

import com.rabbitmq.client.Channel;
import com.shao.po.TransactionMsgPO;
import com.shao.service.TransactionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/4/2 16:16
 */
@Slf4j
public abstract class AbstractMessageListener implements ChannelAwareMessageListener {
    @Autowired
    private TransactionMsgService transactionMsgService;

    @Value("#{new Integer('${rabbitmq.reconsumeTimes}')}")
    private Integer reconsumeTimes;

    public abstract void receiveMessage(Message message);

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        final MessageProperties messageProperties = message.getMessageProperties();
        final long deliveryTag = messageProperties.getDeliveryTag();
        final TransactionMsgPO transactionMsgPO = transactionMsgService.messageReceiveReady(messageProperties.getMessageId(),
                messageProperties.getReceivedExchange(), messageProperties.getReceivedRoutingKey(), messageProperties.getConsumerQueue(),
                new String(message.getBody()));
        log.info("收到消息，当前消息ID={},消费次数={}", messageProperties.getMessageId(), transactionMsgPO.getSequence());
        try {
            receiveMessage(message);
            channel.basicAck(deliveryTag, false);
            transactionMsgService.messageReceiveSuccess(messageProperties.getMessageId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (transactionMsgPO.getSequence() > reconsumeTimes) {
                // 添加到死信队列
                channel.basicReject(deliveryTag, false);
            } else {
                // 重回队列，重复消费，按照2的指数幂进行等待
                Thread.sleep((long) (Math.pow(2, transactionMsgPO.getSequence()) * 1000));
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }
}
