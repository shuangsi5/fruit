package com.fruittrade.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 规则预警引擎：综合等级 = max(临期等级, 损耗等级, 库存不足等级)。
 * 纯计算，便于独立单测。
 */
@Component
public class RuleBasedWarningEngine {

    @Value("${warning.expiring-thresholds:30,7,3}")
    private String expiringThresholds;

    @Value("${warning.loss-risk-thresholds:0.2,0.4,0.7}")
    private String lossRiskThresholds;

    public RuleBasedWarningEngine() {
        this.expiringThresholds = "30,7,3";
        this.lossRiskThresholds = "0.2,0.4,0.7";
    }

    /** 临期等级：remainingDays >30=0; (7,30]=1; (3,7]=2; <=3=3 */
    public int expiringLevel(int remainingDays) {
        if (remainingDays > 30) {
            return 0;
        } else if (remainingDays > 7) {
            return 1;
        } else if (remainingDays > 3) {
            return 2;
        }
        return 3;
    }

    /** 损耗等级：<=0.2=0; (0.2,0.4]=1; (0.4,0.7]=2; >0.7=3 */
    public int lossLevel(double risk) {
        if (risk <= 0.2) {
            return 0;
        } else if (risk <= 0.4) {
            return 1;
        } else if (risk <= 0.7) {
            return 2;
        }
        return 3;
    }

    /** 库存不足等级：stock<=0=3; stock<=safety=2; 否则=0 */
    public int stockLevel(int stock, int safetyStock) {
        if (stock <= 0) {
            return 3;
        } else if (stock <= safetyStock) {
            return 2;
        }
        return 0;
    }

    /**
     * 综合评估。
     *
     * @param productionDate 生产日期
     * @param shelfLifeDays  保质期天数
     * @param stock          可用库存
     * @param safetyStock    安全库存
     * @param today          评估基准日（便于测试）
     * @param q7             近7日销量
     */
    public WarningResult evaluate(LocalDate productionDate, int shelfLifeDays,
                                  int stock, int safetyStock, int q7, LocalDate today) {
        int remainingDays = shelfLifeDays - (int) ChronoUnit.DAYS.between(productionDate, today);
        int expiring = expiringLevel(remainingDays);

        double avg = q7 / 7.0;
        double predicted = avg * Math.max(remainingDays, 0);
        double lossRisk;
        if (remainingDays <= 0 || stock <= 0) {
            lossRisk = 1.0;
        } else {
            lossRisk = Math.max(0.0, (stock - predicted) / (double) stock);
        }
        int loss = lossLevel(lossRisk);
        int stockLv = stockLevel(stock, safetyStock);

        int level = Math.max(expiring, Math.max(loss, stockLv));
        String type;
        if (level == 0) {
            type = "NORMAL";
        } else if (level == stockLv) {
            // 库存售罄/低于安全线时优先展示库存不足
            type = "LOW_STOCK";
        } else if (level == expiring) {
            type = "EXPIRING";
        } else {
            type = "LOSS";
        }
        String suggestion = buildSuggestion(type, level, remainingDays, stock);
        return new WarningResult(level, type, remainingDays, round(lossRisk), expiring, loss, stockLv, suggestion);
    }

    /** 使用当天日期评估 */
    public WarningResult evaluate(LocalDate productionDate, int shelfLifeDays,
                                  int stock, int safetyStock, int q7) {
        return evaluate(productionDate, shelfLifeDays, stock, safetyStock, q7, LocalDate.now());
    }

    /** 等级中文文案（P1 口径：正常/注意/警告/严重） */
    public static String levelText(int level) {
        return switch (level) {
            case 0 -> "正常";
            case 1 -> "注意";
            case 2 -> "警告";
            case 3 -> "严重";
            default -> "未知";
        };
    }

    private String buildSuggestion(String type, int level, int remainingDays, int stock) {
        return switch (type) {
            case "EXPIRING" -> "剩余 " + remainingDays + " 天保质期，建议" +
                    (level >= 3 ? "立即促销清仓或下架" : level == 2 ? "加大促销力度" : "关注临期风险");
            case "LOSS" -> "预计损耗风险较高，建议调整定价或加快周转";
            case "LOW_STOCK" -> stock <= 0 ? "库存已售罄，建议立即补货" : "库存低于安全线，建议及时补货";
            default -> "库存与保质期均健康，无需处理";
        };
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
