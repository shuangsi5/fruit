package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水表实体（日志表，仅保留 create_time，不继承 BaseEntity）。
 */
@Data
@TableName("inventory_log")
public class InventoryLog {

    private Long id;
    private Long productId;
    private Long merchantId;
    private Integer changeType;
    private Integer quantity;
    private Integer beforeStock;
    private Integer afterStock;
    private String refType;
    private Long refId;
    private Long operatorId;
    private String remark;

    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}
