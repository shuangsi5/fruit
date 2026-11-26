package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存表实体。version 字段配合乐观锁插件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity {

    private Long productId;
    private Long merchantId;
    private Integer stock;
    private Integer lockedStock;
    private Integer safetyStock;
    private Integer warningLevel;
    private Integer version;
}
