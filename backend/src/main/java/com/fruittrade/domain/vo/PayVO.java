package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付响应 VO。 */
@Data
public class PayVO {

    private Long orderId;
    private String orderNo;
    private String paymentNo;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime payTime;
}
