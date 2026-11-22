package com.fruittrade.controller;

import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.AfterSaleDTO;
import com.fruittrade.domain.dto.ComplaintDTO;
import com.fruittrade.domain.vo.AfterSaleVO;
import com.fruittrade.domain.vo.ComplaintVO;
import com.fruittrade.domain.vo.RecommendationVO;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.AfterSaleService;
import com.fruittrade.service.ComplaintService;
import com.fruittrade.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端售后、投诉、推荐接口。
 */
@RestController
@RequestMapping("/api")
public class UserFeatureController {

    private final AfterSaleService afterSaleService;
    private final ComplaintService complaintService;
    private final RecommendationService recommendationService;

    public UserFeatureController(AfterSaleService afterSaleService, ComplaintService complaintService,
                                 RecommendationService recommendationService) {
        this.afterSaleService = afterSaleService;
        this.complaintService = complaintService;
        this.recommendationService = recommendationService;
    }

    // 售后
    @PostMapping("/after-sales")
    public Result<Long> applyAfterSale(@Valid @RequestBody AfterSaleDTO dto) {
        return Result.ok(afterSaleService.apply(SecurityUtils.getUserId(), dto));
    }

    @GetMapping("/after-sales")
    public Result<PageResult<AfterSaleVO>> afterSales(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(afterSaleService.userList(SecurityUtils.getUserId(), pageNum, pageSize));
    }

    // 投诉
    @PostMapping("/complaints")
    public Result<Long> createComplaint(@Valid @RequestBody ComplaintDTO dto) {
        return Result.ok(complaintService.create(SecurityUtils.getUserId(), dto));
    }

    // 推荐
    @GetMapping("/recommendations")
    public Result<List<RecommendationVO>> recommendations(
            @RequestParam(value = "scene", defaultValue = "HOME") String scene,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Result.ok(recommendationService.recommend(SecurityUtils.getUserId(), scene, limit));
    }
}
