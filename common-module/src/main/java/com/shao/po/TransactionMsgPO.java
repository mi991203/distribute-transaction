package com.shao.po;

import com.shao.enummeration.TransactionType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/31 15:33
 */
@Data
@Accessors(chain = true)
public class TransactionMsgPO {
    private String id;

    private String service;

    private TransactionType type;

    private String exchange;

    private String routingKey;

    private String queue;

    private Integer sequence;

    private String payload;

    private Date date;
}
