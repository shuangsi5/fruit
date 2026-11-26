package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 商品详情 VO。 */
@Data
public class ProductDetailVO {

    private Long id;
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
    private Integer salesCount;
    private Integer viewCount;
    private Integer status;
    private Long merchantId;
    private String merchantName;
    private Long categoryId;
    private String categoryName;
    private List<ImageVO> images;
    private Integer stock;
}
