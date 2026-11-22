package com.fruittrade.controller;

import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.InventoryUpdateDTO;
import com.fruittrade.domain.dto.ProductSaveDTO;
import com.fruittrade.domain.vo.InventoryVO;
import com.fruittrade.domain.vo.ProductVO;
import com.fruittrade.domain.vo.WarningVO;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.InventoryService;
import com.fruittrade.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家端商品与库存接口（行级隔离：仅本商家数据）。
 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public MerchantProductController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products")
    public Result<PageResult<ProductVO>> products(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(productService.merchantList(SecurityUtils.getMerchantId(), keyword, pageNum, pageSize));
    }

    @PostMapping("/products")
    public Result<Map<String, Object>> create(@Valid @RequestBody ProductSaveDTO dto) {
        Long id = productService.create(SecurityUtils.getMerchantId(), dto);
        return Result.ok(Map.of("productId", id));
    }

    @PutMapping("/products/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        productService.update(SecurityUtils.getMerchantId(), id, dto);
        return Result.ok();
    }

    @PostMapping("/products/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(SecurityUtils.getMerchantId(), id);
        return Result.ok();
    }

    @GetMapping("/products/expiring")
    public Result<List<ProductVO>> expiring(
            @RequestParam(value = "days", defaultValue = "7") int days) {
        return Result.ok(productService.expiring(SecurityUtils.getMerchantId(), days));
    }

    @GetMapping("/inventory")
    public Result<PageResult<InventoryVO>> inventory(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(inventoryService.list(SecurityUtils.getMerchantId(), pageNum, pageSize));
    }

    @PutMapping("/inventory/{productId}")
    public Result<Void> updateInventory(@PathVariable Long productId,
                                        @Valid @RequestBody InventoryUpdateDTO dto) {
        inventoryService.update(SecurityUtils.getMerchantId(), productId, dto);
        return Result.ok();
    }

    @GetMapping("/inventory/warnings")
    public Result<List<WarningVO>> warnings() {
        return Result.ok(inventoryService.warnings(SecurityUtils.getMerchantId()));
    }
}
