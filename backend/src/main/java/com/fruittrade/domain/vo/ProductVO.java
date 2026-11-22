package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 商品列表项 VO。 */
@Data
public class ProductVO {

    private Long id;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private String origin;
    private Integer salesCount;
    private Long merchantId;
    private String merchantName;
    private Integer status;
    private Long categoryId;
    private String categoryName;
    private Integer stock;
}
