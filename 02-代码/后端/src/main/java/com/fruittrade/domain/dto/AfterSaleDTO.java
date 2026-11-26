package com.fruittrade.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后申请请求。
 * 兼容契约字段 productId（前端按契约使用）与 orderItemId。
 */
@Data
public class AfterSaleDTO {

    private Long orderId;

    /** 针对的商品 ID（契约字段） */
    private Long productId;

    /** 针对的订单明细 ID（二选一） */
    private Long orderItemId;

    private Integer type;
    private String reason;
    private String description;
    private List<String> images;
    private BigDecimal amount;
}
