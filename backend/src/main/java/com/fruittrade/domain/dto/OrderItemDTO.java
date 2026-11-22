package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 订单项（下单入参）。 */
@Data
public class OrderItemDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
