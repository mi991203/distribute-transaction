package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.SettlementStatus;
import com.shao.listener.AbstractMessageListener;
import com.shao.mapper.SettlementMapper;
import com.shao.po.SettlementPO;
import com.shao.sender.TransactionMsgSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class SettlementExecuteMsg extends AbstractMessageListener {
    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private TransactionMsgSender transactionMsgSender;

    @Value("${rabbitmq.settlement.exchange}")
    private String settlementExchange;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @Override
    public void receiveMessage(Message message) {
        log.info("settlement-service服务正在消费消息={}", new String(message.getBody()));
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
            transactionMsgSender.send(settlementExchange, orderKey, orderMessageDTO);
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException();
        }
    }
}
