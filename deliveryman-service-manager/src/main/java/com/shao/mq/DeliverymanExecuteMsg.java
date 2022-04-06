package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.DeliverymanStatus;
import com.shao.listener.AbstractMessageListener;
import com.shao.mapper.DeliveryMapper;
import com.shao.po.DeliverymanPO;
import com.shao.sender.TransactionMsgSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 10:33
 */
@Component
@Slf4j
public class DeliverymanExecuteMsg extends AbstractMessageListener {
    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private TransactionMsgSender transactionMsgSender;

    @Value("${rabbitmq.deliveryman.exchange}")
    private String deliveryExchange;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @Override
    public void receiveMessage(Message message) {
        log.info("delivery-service开始消费消息={}", new String(message.getBody()));
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final OrderMessageDTO orderMessageDTO = objectMapper.readValue(message.getBody(), OrderMessageDTO.class);
            final List<DeliverymanPO> deliverymanPOS = deliveryMapper.selectAvailableDeliveryman(DeliverymanStatus.AVALIABLE);
            // 选择一个合适的外卖员，应该是距离最近的，但是目前使用的是随机一个
            DeliverymanPO deliverymanPO = null;
            if (deliverymanPOS.size() != 0) {
                deliverymanPO = deliverymanPOS.get(ThreadLocalRandom.current().nextInt(deliverymanPOS.size()));
            } else {
                log.error("没有合适的外卖员，消息重回队列");
                throw new RuntimeException("没有合适的外卖员，消息重回队列");
            }
            orderMessageDTO.setDeliverymanId(deliverymanPO.getId());
            log.info("delivery-service处理完成消息，准备发送到order-queue队列中，消息内容:{}", orderMessageDTO);
            transactionMsgSender.send(deliveryExchange, orderKey, orderMessageDTO);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException();
        }
    }
}
