package com.fruittrade.domain.dto;

import lombok.Data;

/**
 * 库存调整请求。
 * 兼容前端契约字段 delta(增量)/changeType/remark，以及绝对值 stock/safetyStock。
 */
@Data
public class InventoryUpdateDTO {

    /** 绝对库存值（绝对值模式） */
    private Integer stock;

    /** 库存变动量（增量模式，正为入库、负为出库） */
    private Integer delta;

    /** 变动类型：1入库 2销售出库 3售后回补 4盘点 5锁定 6释放 */
    private Integer changeType;

    private String remark;

    private Integer safetyStock;
}
