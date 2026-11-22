package com.fruittrade;

import com.fruittrade.engine.RuleBasedWarningEngine;
import com.fruittrade.engine.WarningResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** 预警引擎规则单测。 */
class RuleBasedWarningEngineTest {

    private final RuleBasedWarningEngine engine = new RuleBasedWarningEngine();

    @Test
    void testExpiringThresholds() {
        // >30 正常; (7,30] 注意; (3,7] 警告; <=3 严重
        assertEquals(0, engine.expiringLevel(31));
        assertEquals(1, engine.expiringLevel(30));
        assertEquals(1, engine.expiringLevel(8));
        assertEquals(2, engine.expiringLevel(7));
        assertEquals(2, engine.expiringLevel(4));
        assertEquals(3, engine.expiringLevel(3));
        assertEquals(3, engine.expiringLevel(0));
    }

    @Test
    void testLossRiskThresholds() {
        assertEquals(0, engine.lossLevel(0.2));
        assertEquals(1, engine.lossLevel(0.3));
        assertEquals(2, engine.lossLevel(0.5));
        assertEquals(2, engine.lossLevel(0.7));
        assertEquals(3, engine.lossLevel(0.71));
    }

    @Test
    void testStockLevel() {
        assertEquals(3, engine.stockLevel(0, 10));
        assertEquals(2, engine.stockLevel(10, 10));
        assertEquals(2, engine.stockLevel(5, 10));
        assertEquals(0, engine.stockLevel(11, 10));
    }

    @Test
    void testComprehensiveLevelTakesMax() {
        LocalDate today = LocalDate.of(2026, 11, 21);
        // 临期2天(等级3)、库存充足、损耗低 -> 综合取3，类型 EXPIRING
        WarningResult r = engine.evaluate(
                LocalDate.of(2026, 11, 6), 15, 500, 10, 0, today);
        assertEquals(3, r.getLevel());
        assertEquals("EXPIRING", r.getWarningType());
        assertTrue(r.getSuggestion().contains("保质期"));

        // 库存为0 -> 综合3，类型 LOW_STOCK
        WarningResult r2 = engine.evaluate(
                LocalDate.of(2026, 10, 1), 60, 0, 10, 0, today);
        assertEquals(3, r2.getLevel());
        assertEquals("LOW_STOCK", r2.getWarningType());
    }

    @Test
    void testHealthyProductIsNormal() {
        LocalDate today = LocalDate.of(2026, 11, 21);
        // 生产日期今天、保质期40天(>30)、库存充足 -> 正常
        WarningResult r = engine.evaluate(today, 40, 500, 10, 700, today);
        assertEquals(0, r.getLevel());
        assertEquals("NORMAL", r.getWarningType());
        assertEquals("正常", RuleBasedWarningEngine.levelText(0));
    }
}
