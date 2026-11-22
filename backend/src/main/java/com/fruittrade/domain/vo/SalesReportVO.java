package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 商家销售报表 VO。 */
@Data
public class SalesReportVO {

    private BigDecimal totalSales;
    private Long totalOrders;
    private Long totalItems;
    private List<DailySalesVO> dailySales;
    private List<TopProductVO> topProducts;
}
