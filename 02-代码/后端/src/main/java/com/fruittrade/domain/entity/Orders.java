package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Orders extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long addressId;
    private String receiver;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal payAmount;
    private Integer payMethod;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private String remark;
}
