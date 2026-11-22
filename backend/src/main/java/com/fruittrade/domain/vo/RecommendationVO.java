package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 推荐结果 VO。 */
@Data
public class RecommendationVO {

    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal score;
    private String algorithm;
    private String reason;
}
