package com.shao.po;

import com.shao.enummeration.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/28 9:39
 */
@Getter
@Setter
@ToString
public class ProductPO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer restaurantId;
    private ProductStatus status;
    private Date date;
}
