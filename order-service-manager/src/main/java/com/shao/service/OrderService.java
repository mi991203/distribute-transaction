package com.shao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shao.dao.OrderDetailDao;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.OrderStatus;
import com.shao.po.OrderDetailPO;
import com.shao.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

// 用户关于订单的业务请求
@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderDetailDao orderDetailDao;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.restaurant.exchange}")
    private String restaurantExchange;

    @Value("${rabbitmq.restaurant.key}")
    private String restaurantKey;


    public void createOrder(OrderCreateVO orderCreateVO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderDetailPO orderPO = new OrderDetailPO();
        orderPO.setAddress(orderCreateVO.getAddress());
        orderPO.setAccountId(orderCreateVO.getAccountId());
        orderPO.setProductId(orderCreateVO.getProductId());
        orderPO.setStatus(OrderStatus.ORDER_CREATING);
        orderPO.setDate(new Date());
        orderDetailDao.insert(orderPO);

        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        orderMessageDTO.setOrderId(orderPO.getId());
        orderMessageDTO.setProductId(orderPO.getProductId());
        orderMessageDTO.setAccountId(orderCreateVO.getAccountId());
        orderMessageDTO.setConfirmed(true);

        rabbitTemplate.send(restaurantExchange, restaurantKey,
                new Message(objectMapper.writeValueAsBytes(orderMessageDTO), new MessageProperties()),
                new CorrelationData(orderMessageDTO.getOrderId() + ""));
    }

}
