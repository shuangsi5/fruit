package com.fruittrade.controller;

import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.dto.ShipDTO;
import com.fruittrade.domain.vo.AfterSaleVO;
import com.fruittrade.domain.vo.OrderVO;
import com.fruittrade.domain.vo.SalesReportVO;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.AfterSaleService;
import com.fruittrade.service.OrderService;
import com.fruittrade.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端订单、售后、报表接口。
 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantOrderController {

    private final OrderService orderService;
    private final AfterSaleService afterSaleService;
    private final ReportService reportService;

    public MerchantOrderController(OrderService orderService, AfterSaleService afterSaleService,
                                   ReportService reportService) {
        this.orderService = orderService;
        this.afterSaleService = afterSaleService;
        this.reportService = reportService;
    }

    @GetMapping("/orders")
    public Result<PageResult<OrderVO>> orders(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(orderService.merchantList(SecurityUtils.getMerchantId(), status, pageNum, pageSize));
    }

    @PostMapping("/orders/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @RequestBody(required = false) ShipDTO dto) {
        // carrier/logisticsNo 可选：前端可指定，缺省时后端自动生成
        orderService.ship(SecurityUtils.getMerchantId(), id,
                dto == null ? null : dto.getCarrier(),
                dto == null ? null : dto.getLogisticsNo());
        return Result.ok();
    }

    @GetMapping("/after-sales")
    public Result<PageResult<AfterSaleVO>> afterSales(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(afterSaleService.merchantList(SecurityUtils.getMerchantId(), pageNum, pageSize));
    }

    @PostMapping("/after-sales/{id}/handle")
    public Result<Void> handleAfterSale(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        afterSaleService.handle(SecurityUtils.getMerchantId(), id, dto, SecurityUtils.getUserId());
        return Result.ok();
    }

    @GetMapping("/reports/sales")
    public Result<SalesReportVO> salesReport() {
        return Result.ok(reportService.sales(SecurityUtils.getMerchantId()));
    }
}
