package com.fruittrade.domain.dto;

import lombok.Data;

/** 发货请求（可选字段，缺省时后端自动生成）。 */
@Data
public class ShipDTO {

    /** 承运商，如 顺丰速运 */
    private String carrier;

    /** 运单号 */
    private String logisticsNo;
}
