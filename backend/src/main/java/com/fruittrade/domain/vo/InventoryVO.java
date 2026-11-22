package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 库存 VO。 */
@Data
public class InventoryVO {

    private Long id;
    private Long productId;
    private String productName;
    private Long merchantId;
    private Integer stock;
    private Integer lockedStock;
    private Integer safetyStock;
    private Integer warningLevel;
    private String warningText;
}
