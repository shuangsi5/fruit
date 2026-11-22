package com.fruittrade.controller;

import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import com.fruittrade.domain.vo.CategoryVO;
import com.fruittrade.domain.vo.ProductDetailVO;
import com.fruittrade.domain.vo.ProductVO;
import com.fruittrade.domain.vo.RecommendationVO;
import com.fruittrade.service.CategoryService;
import com.fruittrade.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 公共接口：分类列表（树形）、商品列表、商品详情、相关推荐。
 */
@RestController
@RequestMapping("/api")
public class ProductController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public ProductController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories(
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId) {
        return Result.ok(categoryService.tree(parentId));
    }

    @GetMapping("/products")
    public Result<PageResult<ProductVO>> products(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(productService.list(categoryId, keyword, merchantId, minPrice, maxPrice,
                sortBy, pageNum, pageSize));
    }

    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @GetMapping("/products/{id}/related")
    public Result<List<RecommendationVO>> related(
            @PathVariable Long id,
            @RequestParam(value = "limit", defaultValue = "6") int limit) {
        return Result.ok(productService.related(id, limit));
    }
}
