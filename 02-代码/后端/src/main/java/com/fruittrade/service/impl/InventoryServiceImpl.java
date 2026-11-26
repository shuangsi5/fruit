package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.InventoryUpdateDTO;
import com.fruittrade.domain.entity.Inventory;
import com.fruittrade.domain.entity.InventoryLog;
import com.fruittrade.domain.entity.Product;
import com.fruittrade.domain.vo.InventoryVO;
import com.fruittrade.domain.vo.WarningVO;
import com.fruittrade.engine.RuleBasedWarningEngine;
import com.fruittrade.engine.WarningResult;
import com.fruittrade.mapper.InventoryLogMapper;
import com.fruittrade.mapper.InventoryMapper;
import com.fruittrade.mapper.ProductMapper;
import com.fruittrade.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存服务实现（商家库存查询、调整、实时预警计算）。
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final RuleBasedWarningEngine warningEngine;

    public InventoryServiceImpl(InventoryMapper inventoryMapper, ProductMapper productMapper,
                                InventoryLogMapper inventoryLogMapper, RuleBasedWarningEngine warningEngine) {
        this.inventoryMapper = inventoryMapper;
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.warningEngine = warningEngine;
    }

    @Override
    public PageResult<InventoryVO> list(Long merchantId, Long pageNum, Long pageSize) {
        IPage<Inventory> page = inventoryMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getMerchantId, merchantId)
                        .eq(Inventory::getDeleted, 0)
                        .orderByAsc(Inventory::getWarningLevel)
                        .orderByDesc(Inventory::getId));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional
    public void update(Long merchantId, Long productId, InventoryUpdateDTO dto) {
        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, productId)
                        .eq(Inventory::getMerchantId, merchantId)
                        .eq(Inventory::getDeleted, 0)
                        .last("limit 1"));
        if (inv == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "库存记录不存在或非本商家商品");
        }
        int before = inv.getStock() == null ? 0 : inv.getStock();
        // 兼容两种模式：前端 delta 增量 / 绝对值 stock
        int after;
        int changeType;
        if (dto.getDelta() != null) {
            after = before + dto.getDelta();
            changeType = dto.getChangeType() == null ? 4 : dto.getChangeType();
        } else if (dto.getStock() != null) {
            after = dto.getStock();
            changeType = dto.getChangeType() == null ? 4 : dto.getChangeType();
        } else {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "stock 或 delta 至少提供其一");
        }
        if (after < 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "调整后库存不能为负");
        }
        inv.setStock(after);
        if (dto.getSafetyStock() != null) {
            inv.setSafetyStock(dto.getSafetyStock());
        }
        inventoryMapper.updateById(inv);

        InventoryLog logEntity = new InventoryLog();
        logEntity.setProductId(productId);
        logEntity.setMerchantId(merchantId);
        logEntity.setChangeType(changeType);
        logEntity.setQuantity(after - before);
        logEntity.setBeforeStock(before);
        logEntity.setAfterStock(after);
        logEntity.setRefType("MANUAL");
        logEntity.setOperatorId(null);
        logEntity.setRemark(dto.getRemark() == null ? "商家手动调整库存" : dto.getRemark());
        logEntity.setCreateTime(LocalDateTime.now());
        inventoryLogMapper.insert(logEntity);
    }

    @Override
    public List<WarningVO> warnings(Long merchantId) {
        List<Inventory> invs = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getMerchantId, merchantId)
                        .eq(Inventory::getDeleted, 0));
        return invs.stream().map(inv -> {
            Product p = productMapper.selectById(inv.getProductId());
            WarningVO vo = new WarningVO();
            vo.setProductId(inv.getProductId());
            vo.setProductName(p == null ? null : p.getName());
            vo.setStock(inv.getStock());
            if (p != null && p.getProductionDate() != null) {
                WarningResult r = warningEngine.evaluate(p.getProductionDate(), p.getShelfLifeDays(),
                        inv.getStock(), inv.getSafetyStock(), 0);
                vo.setWarningLevel(r.getLevel());
                vo.setWarningText(RuleBasedWarningEngine.levelText(r.getLevel()));
                vo.setWarningType(r.getWarningType());
                vo.setRemainingDays(r.getRemainingDays());
                vo.setLossRisk(r.getLossRisk());
                vo.setSuggestion(r.getSuggestion());
            } else {
                vo.setWarningLevel(inv.getWarningLevel() == null ? 0 : inv.getWarningLevel());
                vo.setWarningText(RuleBasedWarningEngine.levelText(vo.getWarningLevel()));
                vo.setSuggestion("暂无生产日期信息，无法计算临期预警");
            }
            return vo;
        }).sorted((a, b) -> Integer.compare(b.getWarningLevel(), a.getWarningLevel()))
                .collect(Collectors.toList());
    }

    private InventoryVO toVO(Inventory inv) {
        InventoryVO vo = new InventoryVO();
        vo.setId(inv.getId());
        vo.setProductId(inv.getProductId());
        Product p = productMapper.selectById(inv.getProductId());
        vo.setProductName(p == null ? null : p.getName());
        vo.setMerchantId(inv.getMerchantId());
        vo.setStock(inv.getStock());
        vo.setLockedStock(inv.getLockedStock());
        vo.setSafetyStock(inv.getSafetyStock());
        vo.setWarningLevel(inv.getWarningLevel());
        vo.setWarningText(RuleBasedWarningEngine.levelText(inv.getWarningLevel() == null ? 0 : inv.getWarningLevel()));
        return vo;
    }
}
