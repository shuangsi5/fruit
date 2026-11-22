package com.fruittrade.controller;

import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.dto.UserStatusDTO;
import com.fruittrade.domain.vo.*;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 平台管理端接口：商家审核、用户管理、投诉处理、数据总览。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MerchantService merchantService;
    private final UserService userService;
    private final ComplaintService complaintService;
    private final ReportService reportService;

    public AdminController(MerchantService merchantService, UserService userService,
                           ComplaintService complaintService, ReportService reportService) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.complaintService = complaintService;
        this.reportService = reportService;
    }

    @GetMapping("/merchants")
    public Result<PageResult<MerchantVO>> merchants(
            @RequestParam(value = "auditStatus", required = false) Integer auditStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(merchantService.list(auditStatus, pageNum, pageSize));
    }

    @PostMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        merchantService.audit(id, dto, SecurityUtils.getUserId());
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(userService.list(role, pageNum, pageSize));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @Valid @RequestBody UserStatusDTO dto) {
        userService.updateStatus(id, dto.getStatus());
        return Result.ok();
    }

    @GetMapping("/complaints")
    public Result<PageResult<ComplaintVO>> complaints(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.ok(complaintService.list(status, pageNum, pageSize));
    }

    @PostMapping("/complaints/{id}/handle")
    public Result<java.util.Map<String, Object>> handleComplaint(@PathVariable Long id,
                                                                  @Valid @RequestBody AuditDTO dto) {
        return Result.ok(complaintService.handle(id, dto, SecurityUtils.getUserId()));
    }

    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        return Result.ok(reportService.overview());
    }
}
