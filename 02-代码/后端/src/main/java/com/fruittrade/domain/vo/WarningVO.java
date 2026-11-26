package com.fruittrade.domain.vo;

import lombok.Data;

/** 预警结果 VO。 */
@Data
public class WarningVO {

    private Long productId;
    private String productName;
    private Integer warningLevel;
    private String warningText;
    private String warningType;
    private Integer remainingDays;
    private Double lossRisk;
    private String suggestion;
    private Integer stock;
}
