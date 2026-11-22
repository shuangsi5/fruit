package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物流表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics")
public class Logistics extends BaseEntity {

    private Long orderId;
    private String logisticsNo;
    private String carrier;
    private Integer status;
    private String traces;
    private LocalDateTime shipTime;
    private LocalDateTime deliverTime;
}
