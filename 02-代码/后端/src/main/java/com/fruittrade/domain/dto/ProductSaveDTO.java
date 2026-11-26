package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 商家商品新增/编辑请求。 */
@Data
public class ProductSaveDTO {

    private Long categoryId;
    private String name;
    private String subtitle;
    private String description;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;
    private String unit;
    private String spec;
    private String origin;
    private Integer shelfLifeDays;
    private LocalDate productionDate;
    private Integer status;
}
