package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.shao.dao.OrderDetailDao;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.OrderStatus;
import com.shao.po.OrderDetailPO;
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
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Component
public class OrderMessageService {
    @Value("${rabbitmq.deliveryman.exchange}")
    private String deliverymanExchange;

    @Value("${rabbitmq.settlement.exchange}")
    private String settlementExchange;

    @Value("${rabbitmq.reward.exchange}")
    private String rewardExchange;

    @Value("${rabbitmq.deliveryman.key}")
    private String deliverymanKey;

    @Value("${rabbitmq.settlement.key}")
    private String settlementKey;

    @Value("${rabbitmq.reward.key}")
    private String rewardKey;

    @Autowired
    private OrderDetailDao orderDetailDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "queue.order")
    public void handleMessage(Channel channel, @Payload Message message) {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}, DeliveryTag={}", messageBody, message.getMessageProperties().getDeliveryTag());
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);
            OrderDetailPO orderPO = orderDetailDao.selectOrder(orderMessageDTO.getOrderId());
            switch (orderPO.getStatus()) {
                case ORDER_CREATING:
                    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
                        orderPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderPO.setPrice(orderMessageDTO.getPrice());
                        orderDetailDao.update(orderPO);
                        rabbitTemplate.send(deliverymanExchange, deliverymanKey,
                                new Message(objectMapper.writeValueAsBytes(orderMessageDTO), new MessageProperties()),
                                new CorrelationData(orderMessageDTO.getOrderId() + ""));
                        log.info("消息在order-service服务已经处理完成，状态从ORDER_CREATING变为RESTAURANT_CONFIRMED，即将发送到deliveryman-queue中由deliveryman-service进行处理");
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                        log.info("消息在order-service服务已经处理完成，状态从ORDER_CREATING变为RESTAURANT_CONFIRMED过程中发生错误，状态从ORDER_CREATING变为FAILED");
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    if (null != orderMessageDTO.getDeliverymanId()) {
                        orderPO.setStatus(OrderStatus.DELIVERYMAN_CONFIRMED);
                        orderPO.setDeliverymanId(orderMessageDTO.getDeliverymanId());
                        orderDetailDao.update(orderPO);
                        rabbitTemplate.send(settlementExchange, settlementKey,
                                new Message(objectMapper.writeValueAsBytes(orderMessageDTO), new MessageProperties()),
                                new CorrelationData(orderMessageDTO.getOrderId() + ""));
                        log.info("消息在order-service服务消费完成，状态从RESTAURANT_CONFIRMED变为DELIVERYMAN_CONFIRMED，即将发送到settlement-queue中由settlement-service进行处理");
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                        log.info("消息在order-service服务已经处理完成，状态从RESTAURANT_CONFIRMED变为DELIVERYMAN_CONFIRMED过程中发生错误，状态从RESTAURANT_CONFIRMED变为FAILED");
                    }

                    break;
                case DELIVERYMAN_CONFIRMED:
                    if (null != orderMessageDTO.getSettlementId()) {
                        orderPO.setStatus(OrderStatus.SETTLEMENT_CONFIRMED);
                        orderPO.setSettlementId(orderMessageDTO.getSettlementId());
                        orderDetailDao.update(orderPO);
                        rabbitTemplate.send(rewardExchange, rewardKey,
                                new Message(objectMapper.writeValueAsBytes(orderMessageDTO), new MessageProperties()),
                                new CorrelationData(orderMessageDTO.getOrderId() + ""));
                        log.info("消息在order-service服务消费完成，状态从DELIVERYMAN_CONFIRMED变为SETTLEMENT_CONFIRMED，即将发送到reward-queue中由reward-service进行处理");
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }

                    break;
                case SETTLEMENT_CONFIRMED:
                    if (null != orderMessageDTO.getRewardId()) {
                        orderPO.setStatus(OrderStatus.ORDER_CREATED);
                        orderPO.setRewardId(orderMessageDTO.getRewardId());
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                    }
                    orderDetailDao.update(orderPO);
                    break;
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("消息deliveryTag={},消息内容={}，消费失败", message.getMessageProperties().getDeliveryTag(),
                    messageBody);
            log.error("", e);
            // TODO
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

}
