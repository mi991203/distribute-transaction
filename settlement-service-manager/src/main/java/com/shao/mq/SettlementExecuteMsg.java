package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.SettlementStatus;
import com.shao.mapper.SettlementMapper;
import com.shao.po.SettlementPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.MD5Encoder;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 11:26
 */
@Component
@Slf4j
public class SettlementExecuteMsg {
    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.settlement.exchange}")
    private String settlementExchange;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @RabbitListener(queues = {"queue.settlement"})
    public void ConsumeSettlementMsg(Channel channel, @Payload Message message) {
        final String messageBody = new String(message.getBody());
        log.info("settlement-service服务正在消费消息={}", messageBody);
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final OrderMessageDTO orderMessageDTO = objectMapper.readValue(message.getBody(), OrderMessageDTO.class);
            final SettlementPO settlementPO = new SettlementPO();
            settlementPO.setAmount(orderMessageDTO.getPrice())
                    .setDate(new Date())
                    .setOrderId(orderMessageDTO.getOrderId())
                    .setStatus(SettlementStatus.SUCCESS)
                    // 随机生成另一个订单号
                    .setTransactionId(ThreadLocalRandom.current().nextInt(100000000));
            settlementMapper.insert(settlementPO);
            orderMessageDTO.setSettlementId(settlementPO.getId());
            log.info("settlement-service处理完成消息，准备发送到order-queue队列中，消息内容:{}", orderMessageDTO);
            rabbitTemplate.send(settlementExchange, orderKey, new Message(objectMapper.writeValueAsBytes(orderMessageDTO),
                    new MessageProperties()), new CorrelationData(orderMessageDTO.getOrderId() + ""));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("", e);
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
