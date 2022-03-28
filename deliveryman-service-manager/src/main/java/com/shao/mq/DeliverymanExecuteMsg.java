package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.DeliverymanStatus;
import com.shao.mapper.DeliveryMapper;
import com.shao.po.DeliverymanPO;
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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 10:33
 */
@Component
@Slf4j
public class DeliverymanExecuteMsg {
    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.deliveryman.exchange}")
    private String deliveryExchange;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @RabbitListener(queues = {"queue.deliveryman"})
    public void consumeDeliverymanQueue(Channel channel, @Payload Message message) {
        final String messageBody = new String(message.getBody());
        log.info("delivery-service开始消费消息={}", messageBody);
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
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
            orderMessageDTO.setDeliverymanId(deliverymanPO.getId());
            log.info("delivery-service处理完成消息，准备发送到order-queue队列中，消息内容:{}", orderMessageDTO);
            rabbitTemplate.send(deliveryExchange, orderKey, new Message(objectMapper.writeValueAsBytes(orderMessageDTO),
                    new MessageProperties()), new CorrelationData(orderMessageDTO.getOrderId() + ""));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("", e);
            // TODO
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }

    }
}
