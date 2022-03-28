package com.shao.mapper;

import com.shao.po.SettlementPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 13:18
 */
@Mapper
public interface SettlementMapper {

    @Insert("insert into settlement(order_id, transaction_id, amount, status, date) " +
            "values (#{orderId}, #{transactionId}, #{amount}, #{status}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SettlementPO settlementPO);
}
