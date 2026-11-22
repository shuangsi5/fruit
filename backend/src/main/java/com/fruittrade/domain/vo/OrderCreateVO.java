package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 创建订单响应 VO。 */
@Data
public class OrderCreateVO {

    private Long orderId;
    private String orderNo;
    private BigDecimal payAmount;
    private Integer status;
    private LocalDateTime expireTime;
}
