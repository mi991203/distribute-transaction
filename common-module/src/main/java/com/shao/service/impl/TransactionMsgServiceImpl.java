package com.shao.service.impl;

import com.shao.dao.TransactionMsgDao;
import com.shao.enummeration.TransactionType;
import com.shao.po.TransactionMsgPO;
import com.shao.service.TransactionMsgService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/4/1 9:24
 */
@Service
public class TransactionMsgServiceImpl implements TransactionMsgService {
    @Resource
    private TransactionMsgDao transactionMsgDao;

    @Value("${rabbitmq.service}")
    private String serviceName;

    @Override
    public TransactionMsgPO messageSendReady(String exchange, String routingKey, String body) {
        final String messageId = UUID.randomUUID().toString();
        TransactionMsgPO transMessagePO = new TransactionMsgPO();
        transMessagePO.setId(messageId);
        transMessagePO.setService(serviceName);
        transMessagePO.setExchange(exchange);
        transMessagePO.setRoutingKey(routingKey);
        transMessagePO.setPayload(body);
        transMessagePO.setDate(new Date());
        transMessagePO.setSequence(0);
        transMessagePO.setType(TransactionType.SEND);
        transactionMsgDao.insert(transMessagePO);
        return transMessagePO;
    }

    @Override
    public void messageSendSuccess(String id) {
        transactionMsgDao.delete(id, serviceName);
    }

    @Override
    public TransactionMsgPO messageSendReturn(String id, String exchange, String routingKey, String body) {
        TransactionMsgPO transMessagePO = new TransactionMsgPO();
        transMessagePO.setId(UUID.randomUUID().toString());
        transMessagePO.setService(serviceName);
        transMessagePO.setExchange(exchange);
        transMessagePO.setRoutingKey(routingKey);
        transMessagePO.setPayload(body);
        transMessagePO.setDate(new Date());
        transMessagePO.setSequence(0);
        transMessagePO.setType(TransactionType.SEND);
        transactionMsgDao.insert(transMessagePO);
        return transMessagePO;
    }

    @Override
    public void messageResend(String id) {
        // TODO 需要加入分布式锁，因为多个服务节点同时消费会存在冲突
        final TransactionMsgPO transactionMsgPO = transactionMsgDao.selectByIdAndService(id, serviceName);
        transactionMsgPO.setSequence(transactionMsgPO.getSequence() + 1);
        transactionMsgDao.update(transactionMsgPO);
    }

    @Override
    public void messageDead(String id, String exchange, String routingKey, String queue, String body) {
        final TransactionMsgPO transactionMsgPO = new TransactionMsgPO()
                .setId(id)
                .setService(serviceName)
                .setExchange(exchange)
                .setType(TransactionType.DEAD)
                .setRoutingKey(routingKey)
                .setQueue(queue)
                .setSequence(0)
                .setPayload(body)
                .setDate(new Date());
        transactionMsgDao.update(transactionMsgPO);
    }

    @Override
    public void messageDead(String id) {
        final TransactionMsgPO transactionMsgPO = transactionMsgDao.selectByIdAndService(id, serviceName);
        transactionMsgPO.setType(TransactionType.DEAD);
        transactionMsgDao.update(transactionMsgPO);
    }

    @Override
    public TransactionMsgPO messageReceiveReady(String id, String exchange, String routingKey, String queue, String body) {
        // 不需要加分布式锁，因为一个消息只可能发送给一个服务的一个节点
        TransactionMsgPO transactionMsgPO = transactionMsgDao.selectByIdAndService(id, serviceName);
        if (transactionMsgPO == null) {
            transactionMsgPO = new TransactionMsgPO();
            transactionMsgPO.setId(id).setService(serviceName).setType(TransactionType.RECEIVE).setExchange(exchange)
                    .setRoutingKey(routingKey).setQueue(queue).setSequence(0).setPayload(body).setDate(new Date());
            transactionMsgDao.insert(transactionMsgPO);
        } else {
            transactionMsgPO.setSequence(transactionMsgPO.getSequence() + 1);
            transactionMsgDao.update(transactionMsgPO);
        }
        return transactionMsgPO;
    }

    @Override
    public void messageReceiveSuccess(String id) {
        transactionMsgDao.delete(id, serviceName);
    }

    @Override
    public List<TransactionMsgPO> listReadyMessages() {
        return transactionMsgDao.selectByTypeAndService(TransactionType.SEND.name(), serviceName);
    }
}
