package com.fruittrade.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合推荐引擎：score = 0.5*购买频次 + 0.3*User-Based CF + 0.2*近30天热销。
 * 冷启动走热销兜底；带推荐理由。算法为纯计算，便于独立单测。
 */
@Component
public class HybridRecommendationEngine {

    @Value("${recommendation.weight-frequency:0.5}")
    private double weightFrequency;

    @Value("${recommendation.weight-cf:0.3}")
    private double weightCf;

    @Value("${recommendation.weight-hot:0.2}")
    private double weightHot;

    @Value("${recommendation.top-k-similar:20}")
    private int topK;

    @Value("${recommendation.default-limit:10}")
    private int defaultLimit;

    @Value("${recommendation.max-limit:50}")
    private int maxLimit;

    public HybridRecommendationEngine() {
        this.weightFrequency = 0.5;
        this.weightCf = 0.3;
        this.weightHot = 0.2;
        this.topK = 20;
        this.defaultLimit = 10;
        this.maxLimit = 50;
    }

    /** 供测试使用的构造函数 */
    public HybridRecommendationEngine(double wf, double wcf, double wh, int topK, int defaultLimit, int maxLimit) {
        this.weightFrequency = wf;
        this.weightCf = wcf;
        this.weightHot = wh;
        this.topK = topK;
        this.defaultLimit = defaultLimit;
        this.maxLimit = maxLimit;
    }

    /** r(u,i) = 1 + ln(c(u,i))，抑制高频偏差 */
    public double rating(int count) {
        return count <= 0 ? 0.0 : 1.0 + Math.log(count);
    }

    /** 用户 u、v 的余弦相似度（基于共同购买商品） */
    public double cosineSimilarity(Map<Long, Integer> u, Map<Long, Integer> v) {
        if (u == null || v == null || u.isEmpty() || v.isEmpty()) {
            return 0.0;
        }
        double dot = 0.0, normU = 0.0, normV = 0.0;
        for (Map.Entry<Long, Integer> e : u.entrySet()) {
            double ru = rating(e.getValue());
            normU += ru * ru;
            Integer cv = v.get(e.getKey());
            if (cv != null) {
                dot += ru * rating(cv);
            }
        }
        for (Integer cv : v.values()) {
            double rv = rating(cv);
            normV += rv * rv;
        }
        if (normU == 0 || normV == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normU) * Math.sqrt(normV));
    }

    /** 购买频次得分：c(u,i) / max */
    public double frequencyScore(Integer count, int maxCount) {
        if (count == null || count <= 0 || maxCount <= 0) {
            return 0.0;
        }
        return (double) count / maxCount;
    }

    /** 极差归一化 */
    public double minMaxNormalize(double value, double min, double max) {
        if (max - min < 1e-9) {
            return 0.0;
        }
        return (value - min) / (max - min);
    }

    /** 热销得分：s(i) / max */
    public double hotScore(Integer sales, int maxSales) {
        if (sales == null || sales <= 0 || maxSales <= 0) {
            return 0.0;
        }
        return (double) sales / maxSales;
    }

    /** 混合打分 */
    public double score(double freq, double cfNorm, double hot) {
        return weightFrequency * freq + weightCf * cfNorm + weightHot * hot;
    }

    /**
     * 主推荐方法。
     *
     * @param data   算法输入数据
     * @param userId 目标用户
     * @param scene  场景 HOME/DETAIL/CART
     * @param limit  期望返回数量
     * @return 推荐结果（降序）
     */
    public List<RecommendItem> recommend(RecommendData data, Long userId, String scene, int limit) {
        int n = Math.min(Math.max(limit, 1), maxLimit);
        if (n < 1) {
            n = defaultLimit;
        }
        Map<Long, Integer> bought = data.getUserProductCounts().get(userId);
        boolean coldStart = bought == null || bought.isEmpty();

        int maxHot = data.getHotSales().values().stream().max(Integer::compareTo).orElse(0);
        List<Long> candidates = data.candidateIds();

        if (coldStart) {
            return hotFallback(data, candidates, maxHot, n);
        }

        // 频次
        int maxFreq = bought.values().stream().max(Integer::compareTo).orElse(1);
        Map<Long, Double> freqMap = new HashMap<>();
        for (Long pid : candidates) {
            freqMap.put(pid, frequencyScore(bought.get(pid), maxFreq));
        }

        // User-Based CF
        Map<Long, Double> cfRaw = computeCf(data, userId, bought, candidates);
        double cfMin = cfRaw.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double cfMax = cfRaw.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        // 混分
        List<RecommendItem> result = new ArrayList<>();
        for (Long pid : candidates) {
            double freq = freqMap.getOrDefault(pid, 0.0);
            double cfNorm = minMaxNormalize(cfRaw.getOrDefault(pid, 0.0), cfMin, cfMax);
            double hot = hotScore(data.getHotSales().get(pid), maxHot);
            double s = score(freq, cfNorm, hot);
            String reason = buildReason(freq, cfNorm, hot, bought.containsKey(pid));
            result.add(new RecommendItem(pid, data.getCandidateNames().get(pid),
                    round(s), "MIXED", reason));
        }
        result.sort(Comparator.comparingDouble(RecommendItem::getScore).reversed());

        // 不足则用热销榜补足
        if (result.size() < n) {
            Set<Long> exist = result.stream().map(RecommendItem::getProductId).collect(Collectors.toSet());
            result.addAll(hotFallback(data, candidates, maxHot, n).stream()
                    .filter(item -> !exist.contains(item.getProductId()))
                    .limit(n - result.size())
                    .toList());
        }
        return result.stream().limit(n).toList();
    }

    /** 热销兜底（冷启动） */
    public List<RecommendItem> hotFallback(RecommendData data, List<Long> candidates, int maxHot, int n) {
        return candidates.stream()
                .sorted(Comparator.comparingInt((Long pid) -> data.getHotSales().getOrDefault(pid, 0)).reversed())
                .limit(n)
                .map(pid -> new RecommendItem(pid, data.getCandidateNames().get(pid),
                        round(hotScore(data.getHotSales().get(pid), maxHot)), "HOT",
                        "近30天热销商品，适合新用户参考"))
                .toList();
    }

    /** User-Based 协同过滤：对候选商品计算相似用户加权评分 */
    private Map<Long, Double> computeCf(RecommendData data, Long target,
                                        Map<Long, Integer> bought, List<Long> candidates) {
        Map<Long, Double> cf = new HashMap<>();
        List<Map.Entry<Long, Double>> similar = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Integer>> e : data.getUserProductCounts().entrySet()) {
            if (e.getKey().equals(target)) {
                continue;
            }
            double sim = cosineSimilarity(bought, e.getValue());
            if (sim > 0) {
                similar.add(Map.entry(e.getKey(), sim));
            }
        }
        similar.sort(Comparator.comparingDouble(Map.Entry<Long, Double>::getValue).reversed());
        List<Map.Entry<Long, Double>> top = similar.stream().limit(topK).toList();
        double simSum = top.stream().mapToDouble(Map.Entry::getValue).sum();

        for (Long pid : candidates) {
            if (bought.containsKey(pid) || simSum == 0) {
                cf.put(pid, 0.0);
                continue;
            }
            double sum = 0.0;
            for (Map.Entry<Long, Double> neigh : top) {
                Integer c = data.getUserProductCounts().get(neigh.getKey()).get(pid);
                if (c != null) {
                    sum += neigh.getValue() * rating(c);
                }
            }
            cf.put(pid, sum / simSum);
        }
        return cf;
    }

    private String buildReason(double freq, double cfNorm, double hot, boolean purchased) {
        StringBuilder sb = new StringBuilder();
        if (purchased) {
            sb.append("你常购买该类商品；");
        }
        if (cfNorm > 0.5) {
            sb.append("相似用户也喜欢；");
        }
        if (hot > 0.6) {
            sb.append("近期热销；");
        }
        if (sb.length() == 0) {
            sb.append("综合推荐");
        }
        return sb.toString();
    }

    private double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
