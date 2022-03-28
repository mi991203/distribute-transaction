package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.ProductStatus;
import com.shao.enummeration.RestaurantStatus;
import com.shao.mapper.ProductMapper;
import com.shao.mapper.RestaurantMapper;
import com.shao.po.ProductPO;
import com.shao.po.RestaurantPO;
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

@Component
@Slf4j
public class RestaurantExecuteMsg {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RestaurantMapper restaurantMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @Value("${rabbitmq.restaurant.exchange}")
    private String restaurantExchange;

    @RabbitListener(queues = "queue.restaurant")
    public void consumeRestaurantMsg(Channel channel, @Payload Message message) {
        log.info("restaurant-service服务开始消费消息{}", new String(message.getBody()));
        try {
            final OrderMessageDTO orderMessageDTO = new ObjectMapper().readValue(message.getBody(), OrderMessageDTO.class);
            final ProductPO productPO = productMapper.selectProductById(orderMessageDTO.getProductId());
            final RestaurantPO restaurantPO = restaurantMapper.selectRestaurantById(productPO.getRestaurantId());
            // 校验商家是否处于营业状态并且这个菜品是否可售
            if (ProductStatus.AVALIABLE == productPO.getStatus() && RestaurantStatus.OPEN == restaurantPO.getStatus()) {
                orderMessageDTO.setConfirmed(true);
                orderMessageDTO.setPrice(productPO.getPrice());
            } else {
                orderMessageDTO.setConfirmed(false);
            }
            log.info("restaurant-service处理完成消息，准备发送到order-queue队列中，消息内容:{}", orderMessageDTO);
            rabbitTemplate.send(restaurantExchange, orderKey, new Message(new ObjectMapper().writeValueAsBytes(orderMessageDTO)
                    , new MessageProperties()), new CorrelationData(orderMessageDTO.getOrderId() + ""));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.info("", e);
            // TODO 如果消费失败，这条消息该如何处置
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}