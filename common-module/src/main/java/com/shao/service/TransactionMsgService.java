package com.shao.service;

import com.shao.po.TransactionMsgPO;

import java.util.List;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/4/1 9:20
 */
public interface TransactionMsgService {

    TransactionMsgPO messageSendReady(String exchange, String routingKey, String body);

    void messageSendSuccess(String id);

    TransactionMsgPO messageSendReturn(String id, String exchange, String routingKey, String body);

    void messageResend(String id);

    void messageDead(String id, String exchange, String routingKey, String queue, String body);

    void messageDead(String id);

    TransactionMsgPO messageReceiveReady(String id, String exchange, String routingKey, String queue, String body);

    void messageReceiveSuccess(String id);

    List<TransactionMsgPO> listReadyMessages();


}
