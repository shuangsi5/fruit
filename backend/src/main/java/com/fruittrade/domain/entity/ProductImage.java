package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品图片表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_image")
public class ProductImage extends BaseEntity {

    private Long productId;
    private String url;
    private Integer sort;
    private Integer isMain;
}
