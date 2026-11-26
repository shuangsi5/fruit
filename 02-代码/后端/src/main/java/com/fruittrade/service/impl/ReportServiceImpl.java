package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.domain.entity.*;
import com.fruittrade.domain.vo.*;
import com.fruittrade.mapper.*;
import com.fruittrade.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现（商家销售报表、平台数据总览）。
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final SysUserMapper sysUserMapper;
    private final MerchantMapper merchantMapper;
    private final ComplaintMapper complaintMapper;
    private final InventoryMapper inventoryMapper;

    public ReportServiceImpl(OrdersMapper ordersMapper, OrderItemMapper orderItemMapper,
                             ProductMapper productMapper, SysUserMapper sysUserMapper,
                             MerchantMapper merchantMapper, ComplaintMapper complaintMapper,
                             InventoryMapper inventoryMapper) {
        this.ordersMapper = ordersMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.sysUserMapper = sysUserMapper;
        this.merchantMapper = merchantMapper;
        this.complaintMapper = complaintMapper;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public SalesReportVO sales(Long merchantId) {
        // 有效销售订单：状态 1/2/3/5（已支付）
        List<Orders> paid = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getMerchantId, merchantId)
                        .in(Orders::getStatus, 1, 2, 3, 5)
                        .eq(Orders::getDeleted, 0));
        BigDecimal totalSales = paid.stream()
                .map(o -> o.getPayAmount() == null ? BigDecimal.ZERO : o.getPayAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按日聚合
        Map<String, DailyAgg> dailyMap = new LinkedHashMap<>();
        for (Orders o : paid) {
            if (o.getCreateTime() == null) continue;
            String date = o.getCreateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            DailyAgg agg = dailyMap.computeIfAbsent(date, k -> new DailyAgg());
            agg.amount = agg.amount.add(o.getPayAmount() == null ? BigDecimal.ZERO : o.getPayAmount());
            agg.orders++;
        }
        List<DailySalesVO> daily = dailyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    DailySalesVO vo = new DailySalesVO();
                    vo.setDate(e.getKey());
                    vo.setAmount(e.getValue().amount);
                    vo.setOrders(e.getValue().orders);
                    return vo;
                }).collect(Collectors.toList());

        // 热销商品 Top5
        List<Long> orderIds = paid.stream().map(Orders::getId).collect(Collectors.toList());
        Map<Long, TopProductVO> topMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            for (OrderItem oi : items) {
                TopProductVO vo = topMap.computeIfAbsent(oi.getProductId(), k -> {
                    TopProductVO t = new TopProductVO();
                    t.setProductId(oi.getProductId());
                    Product p = productMapper.selectById(oi.getProductId());
                    t.setProductName(p == null ? oi.getProductName() : p.getName());
                    t.setQuantity(0L);
                    t.setAmount(BigDecimal.ZERO);
                    return t;
                });
                vo.setQuantity(vo.getQuantity() + (oi.getQuantity() == null ? 0 : oi.getQuantity()));
                vo.setAmount(vo.getAmount().add(oi.getSubtotal() == null ? BigDecimal.ZERO : oi.getSubtotal()));
            }
        }
        List<TopProductVO> top = topMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getQuantity(), a.getQuantity()))
                .limit(5).collect(Collectors.toList());

        SalesReportVO report = new SalesReportVO();
        report.setTotalSales(totalSales);
        report.setTotalOrders((long) paid.size());
        report.setTotalItems(top.stream().mapToLong(TopProductVO::getQuantity).sum());
        report.setDailySales(daily);
        report.setTopProducts(top);
        return report;
    }

    @Override
    public OverviewVO overview() {
        OverviewVO vo = new OverviewVO();
        vo.setUserCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "USER").eq(SysUser::getDeleted, 0)));
        vo.setMerchantCount(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getDeleted, 0)));
        vo.setPendingMerchantCount(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getAuditStatus, 0).eq(Merchant::getDeleted, 0)));
        vo.setProductCount(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getDeleted, 0)));
        List<Orders> paid = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .in(Orders::getStatus, 1, 2, 3, 5)
                        .eq(Orders::getDeleted, 0));
        vo.setOrderCount((long) paid.size());
        vo.setTotalSales(paid.stream()
                .map(o -> o.getPayAmount() == null ? BigDecimal.ZERO : o.getPayAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setPendingComplaintCount(complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, 0).eq(Complaint::getDeleted, 0)));
        vo.setWarningCount(inventoryMapper.selectCount(
                new LambdaQueryWrapper<Inventory>()
                        .ge(Inventory::getWarningLevel, 1)
                        .eq(Inventory::getDeleted, 0)));
        return vo;
    }

    private static class DailyAgg {
        BigDecimal amount = BigDecimal.ZERO;
        long orders = 0;
    }
}
