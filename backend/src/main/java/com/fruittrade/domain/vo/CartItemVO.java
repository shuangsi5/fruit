package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 购物车项 VO。 */
@Data
public class CartItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private Integer stock;
    private BigDecimal subtotal;
}
