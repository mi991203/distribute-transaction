package com.shao.mapper;

import com.shao.po.RewardPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 14:02
 */
@Mapper
public interface RewardMapper {
    @Insert("INSERT INTO reward(order_id, amount, status, date) VALUES " +
            "(#{orderId}, #{amount}, #{status}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RewardPO rewardPO);
}
