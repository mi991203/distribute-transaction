package com.shao.dao;

import com.shao.po.TransactionMsgPO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/31 15:38
 */
@Mapper
@Repository
public interface TransactionMsgDao {
    @Insert("INSERT INTO transaction_msg(id, service, exchange, type, routing_key, queue, sequence, payload, date) " +
            "VALUES (#{id}, #{service}, #{exchange}, #{type}, #{routingKey}, #{queue}, #{sequence}, #{payload}, #{date})")
    void insert(TransactionMsgPO transactionMsgPO);

    @Update("update transaction_msg " +
            "set id=#{id}, service=#{service},exchange=#{exchange}, type=#{type}, routing_key=#{routingKey}, queue=#{queue}, sequence=#{sequence}, " +
            "payload=#{payload}, date=#{date} " +
            "where id=#{id} and service =#{service}")
    void update(TransactionMsgPO transactionMsgPO);

    @Select("select id, service, exchange, type, routing_key as routingKey, queue, sequence, payload, date from transaction_msg " +
            "where id=#{id} and service=#{service}")
    TransactionMsgPO selectByIdAndService(@Param("id") String id, @Param("service") String service);

    @Select("select id, service,exchange, type, routing_key as routingKey, queue, sequence, payload, date from transaction_msg " +
            "where id=#{type} and service=#{service}")
    List<TransactionMsgPO> selectByTypeAndService(@Param("type") String type, @Param("service") String service);

    @Delete("delete from transaction_msg where id=#{id} and service=#{service}")
    void delete(@Param("id") String id, @Param("service") String service);
}
