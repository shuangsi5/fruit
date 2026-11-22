package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 热销商品 VO。 */
@Data
public class TopProductVO {

    private Long productId;
    private String productName;
    private Long quantity;
    private BigDecimal amount;
}
