package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long merchantId;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private String spec;
    private String origin;
    private Integer shelfLifeDays;
    private LocalDate productionDate;
    private Integer status;
    private Integer salesCount;
    private Integer viewCount;
}
