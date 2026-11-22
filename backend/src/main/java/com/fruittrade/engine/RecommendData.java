package com.fruittrade.engine;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐算法输入数据（由 Service 层从数据库加载后传入，便于纯算法单测）。
 */
@Data
public class RecommendData {

    /** 每个用户对每个商品的购买次数：userId -> (productId -> count) */
    private Map<Long, Map<Long, Integer>> userProductCounts = new HashMap<>();

    /** 近 30 天各商品销量：productId -> sales */
    private Map<Long, Integer> hotSales = new HashMap<>();

    /** 候选商品（已过滤：上架、有库存、未临期）：productId -> 商品名 */
    private Map<Long, String> candidateNames = new HashMap<>();

    public RecommendData() {
    }

    public static RecommendData empty() {
        return new RecommendData();
    }

    public void addPurchase(Long userId, Long productId, int count) {
        userProductCounts.computeIfAbsent(userId, k -> new HashMap<>())
                .merge(productId, count, Integer::sum);
    }

    public void addCandidate(Long productId, String name) {
        candidateNames.put(productId, name);
    }

    public List<Long> candidateIds() {
        return new ArrayList<>(candidateNames.keySet());
    }
}
