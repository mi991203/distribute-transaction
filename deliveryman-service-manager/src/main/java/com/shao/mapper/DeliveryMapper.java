package com.shao.mapper;

import com.shao.enummeration.DeliverymanStatus;
import com.shao.po.DeliverymanPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 10:44
 */
@Mapper
public interface DeliveryMapper {
    @Select("select id, name, status, date from deliveryman where status = #{status}")
    List<DeliverymanPO> selectAvailableDeliveryman(@Param("status") DeliverymanStatus deliverymanStatus);
}
