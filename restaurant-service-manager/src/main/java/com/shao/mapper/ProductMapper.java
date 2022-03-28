package com.shao.mapper;

import com.shao.po.ProductPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 9:40
 */
@Mapper
public interface ProductMapper {
    @Select("select id, name, price, restaurant_id as restaurantId, status, date from product where id = #{id}")
    ProductPO selectProductById(@Param("id") Integer id);
}
