package com.shao.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shao.dto.OrderMessageDTO;
import com.shao.enummeration.RewardStatus;
import com.shao.listener.AbstractMessageListener;
import com.shao.mapper.RewardMapper;
import com.shao.po.RewardPO;
import com.shao.sender.TransactionMsgSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class RewardExecuteMsg extends AbstractMessageListener {
    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private TransactionMsgSender transactionMsgSender;

    @Value("${rabbitmq.reward.exchange}")
    private String rewardExchange;

    @Value("${rabbitmq.order.key}")
    private String orderKey;

    @Override
    public void receiveMessage(Message message) {
        log.info("reward-service服务开始消费消息={}", new String(message.getBody()));
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final OrderMessageDTO orderMessageDTO = objectMapper.readValue(message.getBody(), OrderMessageDTO.class);
            final RewardPO rewardPO = new RewardPO()
                    .setOrderId(orderMessageDTO.getOrderId())
                    .setDate(new Date())
                    .setAmount(orderMessageDTO.getPrice())
                    .setStatus(RewardStatus.SUCCESS);
            rewardMapper.insert(rewardPO);
            orderMessageDTO.setRewardId(rewardPO.getId());
            log.info("reward-service服务已消费完消息，准备发送到order-queue, message={}", orderMessageDTO);
            transactionMsgSender.send(rewardExchange, orderKey, orderMessageDTO);
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException();
        }
    }
}
