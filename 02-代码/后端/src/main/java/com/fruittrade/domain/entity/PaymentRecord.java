package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer payMethod;
    private Integer status;
    private String tradeNo;
    private LocalDateTime paidTime;
}
