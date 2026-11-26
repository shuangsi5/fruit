package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.ProductSaveDTO;
import com.fruittrade.domain.entity.*;
import com.fruittrade.domain.vo.*;
import com.fruittrade.engine.RecommendData;
import com.fruittrade.engine.RecommendItem;
import com.fruittrade.engine.HybridRecommendationEngine;
import com.fruittrade.mapper.*;
import com.fruittrade.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现（前台浏览、详情、相关推荐、商家商品管理）。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final InventoryMapper inventoryMapper;
    private final MerchantMapper merchantMapper;
    private final CategoryMapper categoryMapper;
    private final HybridRecommendationEngine recommendationEngine;

    public ProductServiceImpl(ProductMapper productMapper, ProductImageMapper productImageMapper,
                              InventoryMapper inventoryMapper, MerchantMapper merchantMapper,
                              CategoryMapper categoryMapper, HybridRecommendationEngine recommendationEngine) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.inventoryMapper = inventoryMapper;
        this.merchantMapper = merchantMapper;
        this.categoryMapper = categoryMapper;
        this.recommendationEngine = recommendationEngine;
    }

    @Override
    public PageResult<ProductVO> list(Long categoryId, String keyword, Long merchantId,
                                      BigDecimal minPrice, BigDecimal maxPrice, String sortBy,
                                      Long pageNum, Long pageSize) {
        LambdaQueryWrapper<Product> q = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0);
        if (categoryId != null) {
            q.eq(Product::getCategoryId, categoryId);
        }
        if (merchantId != null) {
            q.eq(Product::getMerchantId, merchantId);
        }
        if (keyword != null && !keyword.isBlank()) {
            q.like(Product::getName, keyword);
        }
        if (minPrice != null) {
            q.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            q.le(Product::getPrice, maxPrice);
        }
        String sort = sortBy == null ? "newest" : sortBy;
        switch (sort) {
            case "sales" -> q.orderByDesc(Product::getSalesCount);
            case "priceAsc" -> q.orderByAsc(Product::getPrice);
            case "priceDesc" -> q.orderByDesc(Product::getPrice);
            default -> q.orderByDesc(Product::getId);
        }
        IPage<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null || !Integer.valueOf(1).equals(p.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在或已下架");
        }
        // 浏览量 +1
        p.setViewCount((p.getViewCount() == null ? 0 : p.getViewCount()) + 1);
        productMapper.updateById(p);

        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setSubtitle(p.getSubtitle());
        vo.setDescription(p.getDescription());
        vo.setPrice(p.getPrice());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setUnit(p.getUnit());
        vo.setSpec(p.getSpec());
        vo.setOrigin(p.getOrigin());
        vo.setShelfLifeDays(p.getShelfLifeDays());
        vo.setProductionDate(p.getProductionDate());
        vo.setSalesCount(p.getSalesCount());
        vo.setViewCount(p.getViewCount());
        vo.setStatus(p.getStatus());
        vo.setMerchantId(p.getMerchantId());
        Merchant m = merchantMapper.selectById(p.getMerchantId());
        vo.setMerchantName(m == null ? null : m.getMerchantName());
        vo.setCategoryId(p.getCategoryId());
        Category c = categoryMapper.selectById(p.getCategoryId());
        vo.setCategoryName(c == null ? null : c.getName());
        vo.setImages(imagesOf(id));

        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, id)
                        .eq(Inventory::getDeleted, 0)
                        .last("limit 1"));
        vo.setStock(inv == null ? 0 : inv.getStock());
        return vo;
    }

    @Override
    public List<RecommendationVO> related(Long id, int limit) {
        Product target = productMapper.selectById(id);
        if (target == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<Product> candidates = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0)
                        .ne(Product::getId, id)
                        .eq(Product::getCategoryId, target.getCategoryId())
                        .orderByDesc(Product::getSalesCount)
                        .last("limit " + Math.max(limit, 1)));
        return candidates.stream().map(p -> {
            RecommendationVO vo = new RecommendationVO();
            vo.setProductId(p.getId());
            vo.setProductName(p.getName());
            vo.setMainImage(mainImage(p.getId()));
            vo.setPrice(p.getPrice());
            vo.setScore(new BigDecimal("0.7000"));
            vo.setAlgorithm("HOT");
            vo.setReason("同类热销商品，你可能也喜欢");
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<ProductVO> merchantList(Long merchantId, String keyword, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<Product> q = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getId);
        if (keyword != null && !keyword.isBlank()) {
            q.like(Product::getName, keyword);
        }
        IPage<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional
    public Long create(Long merchantId, ProductSaveDTO dto) {
        Product p = new Product();
        applySave(p, dto);
        p.setMerchantId(merchantId);
        p.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        p.setSalesCount(0);
        p.setViewCount(0);
        p.setDeleted(0);
        productMapper.insert(p);

        Inventory inv = new Inventory();
        inv.setProductId(p.getId());
        inv.setMerchantId(merchantId);
        inv.setStock(0);
        inv.setLockedStock(0);
        inv.setSafetyStock(10);
        inv.setWarningLevel(0);
        inv.setVersion(0);
        inv.setDeleted(0);
        inventoryMapper.insert(inv);
        return p.getId();
    }

    @Override
    @Transactional
    public void update(Long merchantId, Long id, ProductSaveDTO dto) {
        Product p = productMapper.selectById(id);
        if (p == null || !p.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "商品不存在或非本商家商品");
        }
        applySave(p, dto);
        if (dto.getStatus() != null) {
            p.setStatus(dto.getStatus());
        }
        productMapper.updateById(p);
    }

    @Override
    public void offShelf(Long merchantId, Long id) {
        Product p = productMapper.selectById(id);
        if (p == null || !p.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        p.setStatus(0);
        productMapper.updateById(p);
    }

    @Override
    public List<ProductVO> expiring(Long merchantId, int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return productMapper.selectList(
                        new LambdaQueryWrapper<Product>()
                                .eq(Product::getMerchantId, merchantId)
                                .eq(Product::getStatus, 1)
                                .eq(Product::getDeleted, 0)
                                .isNotNull(Product::getProductionDate))
                .stream()
                .filter(p -> p.getProductionDate().plusDays(p.getShelfLifeDays()).isBefore(threshold.plusDays(1)))
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private void applySave(Product p, ProductSaveDTO dto) {
        p.setCategoryId(dto.getCategoryId());
        p.setName(dto.getName());
        p.setSubtitle(dto.getSubtitle());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setUnit(dto.getUnit() == null ? "斤" : dto.getUnit());
        p.setSpec(dto.getSpec());
        p.setOrigin(dto.getOrigin());
        p.setShelfLifeDays(dto.getShelfLifeDays() == null ? 7 : dto.getShelfLifeDays());
        p.setProductionDate(dto.getProductionDate());
    }

    private ProductVO toVO(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setSubtitle(p.getSubtitle());
        vo.setMainImage(mainImage(p.getId()));
        vo.setPrice(p.getPrice());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setUnit(p.getUnit());
        vo.setOrigin(p.getOrigin());
        vo.setSalesCount(p.getSalesCount());
        vo.setMerchantId(p.getMerchantId());
        Merchant m = merchantMapper.selectById(p.getMerchantId());
        vo.setMerchantName(m == null ? null : m.getMerchantName());
        vo.setStatus(p.getStatus());
        vo.setCategoryId(p.getCategoryId());
        Category c = categoryMapper.selectById(p.getCategoryId());
        vo.setCategoryName(c == null ? null : c.getName());
        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, p.getId())
                        .eq(Inventory::getDeleted, 0)
                        .last("limit 1"));
        vo.setStock(inv == null ? 0 : inv.getStock());
        return vo;
    }

    private List<ImageVO> imagesOf(Long productId) {
        return productImageMapper.selectList(
                        new LambdaQueryWrapper<ProductImage>()
                                .eq(ProductImage::getProductId, productId)
                                .eq(ProductImage::getDeleted, 0)
                                .orderByAsc(ProductImage::getSort))
                .stream().map(img -> {
                    ImageVO vo = new ImageVO();
                    vo.setUrl(img.getUrl());
                    vo.setSort(img.getSort());
                    vo.setIsMain(img.getIsMain());
                    return vo;
                }).collect(Collectors.toList());
    }

    private String mainImage(Long productId) {
        ProductImage first = productImageMapper.selectOne(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .eq(ProductImage::getIsMain, 1)
                        .eq(ProductImage::getDeleted, 0)
                        .last("limit 1"));
        if (first == null) {
            first = productImageMapper.selectOne(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, productId)
                            .eq(ProductImage::getDeleted, 0)
                            .orderByAsc(ProductImage::getSort)
                            .last("limit 1"));
        }
        return first == null ? null : first.getUrl();
    }
}
