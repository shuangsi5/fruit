package com.fruittrade.controller;

import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.OrderCreateDTO;
import com.fruittrade.domain.dto.PayDTO;
import com.fruittrade.domain.entity.Orders;
import com.fruittrade.domain.vo.*;
import com.fruittrade.mapper.OrdersMapper;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单接口：下单、支付、列表、详情、取消、确认、物流。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrdersMapper ordersMapper;

    public OrderController(OrderService orderService, OrdersMapper ordersMapper) {
        this.orderService = orderService;
        this.ordersMapper = ordersMapper;
    }

    @PostMapping
    public Result<OrderCreateVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(orderService.create(SecurityUtils.getUserId(), dto));
    }

    @PostMapping("/{id}/pay")
    public Result<PayVO> pay(@PathVariable Long id,
                             @RequestBody(required = false) PayDTO dto,
                             @RequestParam(value = "payMethod", required = false) Integer payMethodQuery) {
        // 兼容请求体 JSON { payMethod } 与 query 参数两种传法
        Integer method = (dto != null && dto.getPayMethod() != null) ? dto.getPayMethod() : payMethodQuery;
        return Result.ok(orderService.pay(SecurityUtils.getUserId(), id, method));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(orderService.list(SecurityUtils.getUserId(), status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        // 数据权限：本人订单 / 该订单商家 / 管理员可查看
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        SecurityUtils.requireOwner(order.getUserId(), order.getMerchantId());
        return Result.ok(orderService.detail(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(SecurityUtils.getUserId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(SecurityUtils.getUserId(), id);
        return Result.ok();
    }

    @GetMapping("/{id}/logistics")
    public Result<LogisticsVO> logistics(@PathVariable Long id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        SecurityUtils.requireOwner(order.getUserId(), order.getMerchantId());
        return Result.ok(orderService.logistics(id));
    }
}
