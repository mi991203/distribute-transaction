package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.RewardStatus;
import com.shao.mapper.RewardMapper;
import com.shao.po.RewardPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 14:04
 */
@Component
@Slf4j
public class RewardExecuteMsg {
    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.reward.exchange}")
    private String rewardExchange;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @RabbitListener(queues = {"queue.reward"})
    public void consumeRewardMsg(Channel channel, @Payload Message message) {
        final String messageBody = new String(message.getBody());
        log.info("reward-service服务开始消费消息={}", messageBody);
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            final RewardPO rewardPO = new RewardPO()
                    .setOrderId(orderMessageDTO.getOrderId())
                    .setDate(new Date())
                    .setAmount(orderMessageDTO.getPrice())
                    .setStatus(RewardStatus.SUCCESS);
            rewardMapper.insert(rewardPO);
            orderMessageDTO.setRewardId(rewardPO.getId());
            log.info("reward-service服务已消费完消息，准备发送到order-queue, message={}", orderMessageDTO);
            rabbitTemplate.send(rewardExchange, orderKey, new Message(objectMapper.writeValueAsBytes(orderMessageDTO),
                    new MessageProperties()), new CorrelationData(orderMessageDTO.getOrderId() + ""));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("", e);
            // TODO
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
