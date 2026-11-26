package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 购物车整体 VO。 */
@Data
public class CartVO {

    private List<CartItemVO> items;
    private Integer totalQuantity;
    private BigDecimal selectedSubtotal;
}
