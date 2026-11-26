package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.domain.entity.*;
import com.fruittrade.domain.vo.RecommendationVO;
import com.fruittrade.engine.*;
import com.fruittrade.mapper.*;
import com.fruittrade.service.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现：从数据库加载购买/热销/候选数据，调用混合引擎，本地缓存 30 分钟，写推荐日志。
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final InventoryMapper inventoryMapper;
    private final RecommendationLogMapper recommendationLogMapper;
    private final HybridRecommendationEngine engine;

    @Value("${recommendation.cache-ttl-minutes:30}")
    private long cacheTtlMinutes;

    @Value("${recommendation.purchase-window-days:180}")
    private int purchaseWindowDays;

    /** 本地缓存：reco:{userId}:{scene} -> (结果, 过期时间) */
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public RecommendationServiceImpl(OrdersMapper ordersMapper, OrderItemMapper orderItemMapper,
                                     ProductMapper productMapper, ProductImageMapper productImageMapper,
                                     InventoryMapper inventoryMapper, RecommendationLogMapper recommendationLogMapper,
                                     HybridRecommendationEngine engine) {
        this.ordersMapper = ordersMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.inventoryMapper = inventoryMapper;
        this.recommendationLogMapper = recommendationLogMapper;
        this.engine = engine;
    }

    @Override
    public List<RecommendationVO> recommend(Long userId, String scene, int limit) {
        String key = "reco:" + userId + ":" + (scene == null ? "HOME" : scene);
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expireAt > System.currentTimeMillis()) {
            return entry.value;
        }

        RecommendData data = loadData();
        List<RecommendItem> items = engine.recommend(data, userId, scene, limit);
        List<RecommendationVO> result = items.stream().map(this::toVO).collect(Collectors.toList());

        // 写推荐日志（先清理该用户历史记录，保留最新一次）
        recommendationLogMapper.delete(
                new LambdaQueryWrapper<RecommendationLog>().eq(RecommendationLog::getUserId, userId));
        for (RecommendItem item : items) {
            RecommendationLog logEntity = new RecommendationLog();
            logEntity.setUserId(userId);
            logEntity.setProductId(item.getProductId());
            logEntity.setScore(BigDecimal.valueOf(item.getScore()));
            logEntity.setSource(item.getAlgorithm());
            logEntity.setAlgorithmVersion("v1.0");
            logEntity.setScene(scene == null ? "HOME" : scene);
            logEntity.setCreateTime(java.time.LocalDateTime.now());
            recommendationLogMapper.insert(logEntity);
        }

        cache.put(key, new CacheEntry(result,
                System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(cacheTtlMinutes)));
        return result;
    }

    /** 清除某用户推荐缓存（订单完成/售后变更时调用） */
    public void evictUser(Long userId) {
        cache.keySet().removeIf(k -> k.startsWith("reco:" + userId + ":"));
    }

    private RecommendData loadData() {
        RecommendData data = new RecommendData();

        // 购买记录（近 N 天，状态 1/2/3）
        LocalDate since = LocalDate.now().minusDays(purchaseWindowDays);
        List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .in(Orders::getStatus, 1, 2, 3)
                        .ge(Orders::getCreateTime, since.atStartOfDay())
                        .eq(Orders::getDeleted, 0));
        Map<Long, Long> orderToUser = orders.stream()
                .collect(Collectors.toMap(Orders::getId, Orders::getUserId, (a, b) -> a));
        if (!orderToUser.isEmpty()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .in(OrderItem::getOrderId, orderToUser.keySet())
                            .eq(OrderItem::getDeleted, 0));
            for (OrderItem oi : items) {
                Long uid = orderToUser.get(oi.getOrderId());
                if (uid != null) {
                    data.addPurchase(uid, oi.getProductId(), oi.getQuantity() == null ? 1 : oi.getQuantity());
                }
            }
        }

        // 近 30 天热销（用 sales_count 近似，近期优先）
        LocalDate hotSince = LocalDate.now().minusDays(30);
        List<Orders> hotOrders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .in(Orders::getStatus, 1, 2, 3)
                        .ge(Orders::getCreateTime, hotSince.atStartOfDay())
                        .eq(Orders::getDeleted, 0));
        Map<Long, Integer> hotSales = new HashMap<>();
        if (!hotOrders.isEmpty()) {
            List<OrderItem> hotItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .in(OrderItem::getOrderId, hotOrders.stream().map(Orders::getId).collect(Collectors.toList()))
                            .eq(OrderItem::getDeleted, 0));
            for (OrderItem oi : hotItems) {
                hotSales.merge(oi.getProductId(), oi.getQuantity() == null ? 1 : oi.getQuantity(), Integer::sum);
            }
        }

        // 候选商品：上架、有库存、剩余保质期 > 1 天
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0));
        for (Product p : products) {
            Inventory inv = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getProductId, p.getId())
                            .eq(Inventory::getDeleted, 0)
                            .last("limit 1"));
            if (inv == null || inv.getStock() == null || inv.getStock() <= 0) {
                continue;
            }
            if (p.getProductionDate() != null && p.getShelfLifeDays() != null) {
                long remaining = p.getShelfLifeDays()
                        - ChronoUnit.DAYS.between(p.getProductionDate(), LocalDate.now());
                if (remaining <= 1) {
                    continue;
                }
            }
            data.addCandidate(p.getId(), p.getName());
            data.getHotSales().put(p.getId(), hotSales.getOrDefault(p.getId(), 0));
        }
        return data;
    }

    private RecommendationVO toVO(RecommendItem item) {
        RecommendationVO vo = new RecommendationVO();
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setPrice(BigDecimal.ZERO);
        Product p = productMapper.selectById(item.getProductId());
        if (p != null) {
            vo.setProductName(p.getName());
            vo.setPrice(p.getPrice());
        }
        ProductImage img = productImageMapper.selectOne(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, item.getProductId())
                        .eq(ProductImage::getIsMain, 1)
                        .eq(ProductImage::getDeleted, 0)
                        .last("limit 1"));
        vo.setMainImage(img == null ? null : img.getUrl());
        vo.setScore(BigDecimal.valueOf(item.getScore()));
        vo.setAlgorithm(item.getAlgorithm());
        vo.setReason(item.getReason());
        return vo;
    }

    private static class CacheEntry {
        final List<RecommendationVO> value;
        final long expireAt;

        CacheEntry(List<RecommendationVO> value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}
