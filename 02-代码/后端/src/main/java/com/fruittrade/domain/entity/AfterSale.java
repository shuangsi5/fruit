package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("after_sale")
public class AfterSale extends BaseEntity {

    private String afterSaleNo;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long merchantId;
    private Integer type;
    private String reason;
    private String description;
    private String images;
    private BigDecimal amount;
    private Integer status;
    private String handleRemark;
    private Long handlerId;
    private LocalDateTime handleTime;
}
