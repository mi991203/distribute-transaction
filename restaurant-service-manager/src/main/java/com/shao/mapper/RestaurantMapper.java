package com.shao.mapper;

import com.shao.po.RestaurantPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 9:35
 */
@Mapper
public interface RestaurantMapper {
    @Select("select id, name, address, status, date from restaurant where id = #{id}")
    RestaurantPO selectRestaurantById(@Param("id") Integer id);
}
