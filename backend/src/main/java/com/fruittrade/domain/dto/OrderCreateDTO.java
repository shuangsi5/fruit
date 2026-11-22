package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 创建订单请求。 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemDTO> items;

    private String remark;
    private Integer payMethod;
}
