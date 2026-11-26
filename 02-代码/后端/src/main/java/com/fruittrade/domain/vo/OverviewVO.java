package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 平台数据总览 VO。 */
@Data
public class OverviewVO {

    private Long userCount;
    private Long merchantCount;
    private Long pendingMerchantCount;
    private Long productCount;
    private Long orderCount;
    private BigDecimal totalSales;
    private Long pendingComplaintCount;
    private Long warningCount;
}
