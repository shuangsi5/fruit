package com.fruittrade.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.domain.entity.Inventory;
import com.fruittrade.domain.entity.Product;
import com.fruittrade.engine.RuleBasedWarningEngine;
import com.fruittrade.engine.WarningResult;
import com.fruittrade.mapper.InventoryMapper;
import com.fruittrade.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 定时预警扫描任务：每日扫描在售商品，调用预警引擎计算并回写 warning_level。
 */
@Slf4j
@Component
public class InventoryWarningJob {

    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final RuleBasedWarningEngine warningEngine;

    @Value("${warning.enabled:true}")
    private boolean enabled;

    public InventoryWarningJob(ProductMapper productMapper, InventoryMapper inventoryMapper,
                               RuleBasedWarningEngine warningEngine) {
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.warningEngine = warningEngine;
    }

    @Scheduled(cron = "${warning.scan-cron:0 0 2 * * ?}")
    public void scan() {
        if (!enabled) {
            return;
        }
        try {
            List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getStatus, 1)
                            .eq(Product::getDeleted, 0));
            int updated = 0;
            for (Product product : products) {
                Inventory inv = inventoryMapper.selectOne(
                        new LambdaQueryWrapper<Inventory>()
                                .eq(Inventory::getProductId, product.getId())
                                .eq(Inventory::getDeleted, 0)
                                .last("limit 1"));
                if (inv == null || product.getProductionDate() == null) {
                    continue;
                }
                WarningResult result = warningEngine.evaluate(
                        product.getProductionDate(), product.getShelfLifeDays(),
                        inv.getStock(), inv.getSafetyStock(), 0, LocalDate.now());
                if (inv.getWarningLevel() == null || inv.getWarningLevel() != result.getLevel()) {
                    inv.setWarningLevel(result.getLevel());
                    inventoryMapper.updateById(inv);
                    updated++;
                }
            }
            log.info("预警扫描完成，扫描 {} 个商品，更新 {} 条库存预警", products.size(), updated);
        } catch (Exception e) {
            log.error("预警扫描任务执行失败", e);
        }
    }
}
