package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {

    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer selected;
}
