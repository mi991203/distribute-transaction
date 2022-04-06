package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shao.dao.OrderDetailDao;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.OrderStatus;
import com.shao.listener.AbstractMessageListener;
import com.shao.po.OrderDetailPO;
import com.shao.sender.TransactionMsgSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderMessageService extends AbstractMessageListener {
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
    private TransactionMsgSender transactionMsgSender;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void receiveMessage(Message message) {
        log.info("正在消费message={}", new String(message.getBody()));
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(message.getBody(),
                    OrderMessageDTO.class);
            OrderDetailPO orderPO = orderDetailDao.selectOrder(orderMessageDTO.getOrderId());
            switch (orderPO.getStatus()) {
                case ORDER_CREATING:
                    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
                        orderPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderPO.setPrice(orderMessageDTO.getPrice());
                        orderDetailDao.update(orderPO);
                        transactionMsgSender.send(deliverymanExchange, deliverymanKey, orderMessageDTO);
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
                        transactionMsgSender.send(settlementExchange, settlementKey, orderMessageDTO);
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
                        transactionMsgSender.send(rewardExchange, rewardKey, orderMessageDTO);
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
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}
