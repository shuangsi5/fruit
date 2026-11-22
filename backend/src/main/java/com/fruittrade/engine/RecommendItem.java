package com.fruittrade.engine;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 推荐结果项（引擎输出）。
 */
@Data
@AllArgsConstructor
public class RecommendItem {

    private Long productId;
    private String productName;
    private double score;
    private String algorithm;
    private String reason;
}
