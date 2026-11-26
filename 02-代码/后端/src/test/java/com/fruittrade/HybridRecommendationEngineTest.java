package com.fruittrade;

import com.fruittrade.engine.HybridRecommendationEngine;
import com.fruittrade.engine.RecommendData;
import com.fruittrade.engine.RecommendItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 推荐引擎评分公式单测。 */
class HybridRecommendationEngineTest {

    @Test
    void testMixedScoreFormula() {
        HybridRecommendationEngine engine = new HybridRecommendationEngine(0.5, 0.3, 0.2, 20, 10, 50);
        // score = 0.5*1 + 0.3*0.5 + 0.2*0.8 = 0.5+0.15+0.16 = 0.81
        double s = engine.score(1.0, 0.5, 0.8);
        assertEquals(0.81, s, 1e-9);
    }

    @Test
    void testFrequencyAndHotNormalization() {
        HybridRecommendationEngine engine = new HybridRecommendationEngine();
        assertEquals(1.0, engine.frequencyScore(10, 10), 1e-9);
        assertEquals(0.5, engine.frequencyScore(5, 10), 1e-9);
        assertEquals(0.0, engine.frequencyScore(0, 10), 1e-9);
        assertEquals(1.0, engine.hotScore(200, 200), 1e-9);
        assertEquals(0.5, engine.minMaxNormalize(5, 0, 10), 1e-9);
    }

    @Test
    void testCosineSimilarity() {
        HybridRecommendationEngine engine = new HybridRecommendationEngine();
        RecommendData d = new RecommendData();
        d.addPurchase(1L, 101L, 3);
        d.addPurchase(1L, 102L, 2);
        d.addPurchase(2L, 101L, 3);
        d.addPurchase(2L, 102L, 2);
        double sim = engine.cosineSimilarity(
                d.getUserProductCounts().get(1L), d.getUserProductCounts().get(2L));
        assertEquals(1.0, sim, 1e-9);
    }

    @Test
    void testColdStartHotFallback() {
        HybridRecommendationEngine engine = new HybridRecommendationEngine(0.5, 0.3, 0.2, 20, 10, 50);
        RecommendData data = new RecommendData();
        // 用户 99 无任何购买记录 -> 冷启动
        data.addPurchase(1L, 101L, 5);
        data.addCandidate(101L, "赣南脐橙");
        data.addCandidate(102L, "砂糖橘");
        data.addCandidate(103L, "红富士苹果");
        data.getHotSales().put(101L, 100);
        data.getHotSales().put(102L, 500);  // 最热
        data.getHotSales().put(103L, 300);

        List<RecommendItem> result = engine.recommend(data, 99L, "HOME", 3);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("HOT", result.get(0).getAlgorithm());
        // 热销最高的是 102
        assertEquals(102L, result.get(0).getProductId());
        assertTrue(result.get(0).getReason().contains("热销"));
    }

    @Test
    void testMixedRecommendationWithReason() {
        HybridRecommendationEngine engine = new HybridRecommendationEngine(0.5, 0.3, 0.2, 20, 10, 50);
        RecommendData data = new RecommendData();
        data.addPurchase(1L, 101L, 5);   // 目标用户买过 101
        data.addPurchase(2L, 101L, 4);
        data.addPurchase(2L, 102L, 3);   // 相似用户买过 102
        data.addCandidate(101L, "赣南脐橙");
        data.addCandidate(102L, "砂糖橘");
        data.addCandidate(103L, "苹果");
        data.getHotSales().put(101L, 100);
        data.getHotSales().put(102L, 80);
        data.getHotSales().put(103L, 300);

        List<RecommendItem> result = engine.recommend(data, 1L, "HOME", 3);
        assertEquals(3, result.size());
        assertEquals("MIXED", result.get(0).getAlgorithm());
        assertNotNull(result.get(0).getReason());
        assertFalse(result.get(0).getReason().isEmpty());
    }
}
