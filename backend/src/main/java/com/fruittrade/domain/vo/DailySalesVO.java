package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 每日销售 VO。 */
@Data
public class DailySalesVO {

    private String date;
    private BigDecimal amount;
    private Long orders;
}
