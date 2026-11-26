package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 售后 VO。 */
@Data
public class AfterSaleVO {

    private Long id;
    private String afterSaleNo;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long merchantId;
    private Integer type;
    private String typeText;
    private String reason;
    private String description;
    private BigDecimal amount;
    private Integer status;
    private String statusText;
    private String handleRemark;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}
