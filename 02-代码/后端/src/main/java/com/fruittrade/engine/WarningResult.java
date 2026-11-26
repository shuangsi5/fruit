package com.fruittrade.engine;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 预警评估结果（引擎输出）。 */
@Data
@AllArgsConstructor
public class WarningResult {

    /** 综合等级 0-3 */
    private int level;
    /** 导致最高等级的类型：EXPIRING/LOSS/LOW_STOCK/NORMAL */
    private String warningType;
    /** 剩余保质期天数 */
    private int remainingDays;
    /** 损耗风险 0-1 */
    private double lossRisk;
    /** 临期等级 */
    private int expiringLevel;
    /** 损耗等级 */
    private int lossLevel;
    /** 库存不足等级 */
    private int stockLevel;
    /** 处理建议（中文文案，P1 口径） */
    private String suggestion;
}
