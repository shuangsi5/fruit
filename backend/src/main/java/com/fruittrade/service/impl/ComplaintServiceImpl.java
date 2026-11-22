package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.dto.ComplaintDTO;
import com.fruittrade.domain.entity.AuditRecord;
import com.fruittrade.domain.entity.Complaint;
import com.fruittrade.domain.vo.ComplaintVO;
import com.fruittrade.mapper.AuditRecordMapper;
import com.fruittrade.mapper.ComplaintMapper;
import com.fruittrade.service.ComplaintService;
import com.fruittrade.service.NumberGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投诉服务实现。
 */
@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final AuditRecordMapper auditRecordMapper;

    public ComplaintServiceImpl(ComplaintMapper complaintMapper, AuditRecordMapper auditRecordMapper) {
        this.complaintMapper = complaintMapper;
        this.auditRecordMapper = auditRecordMapper;
    }

    @Override
    public Long create(Long userId, ComplaintDTO dto) {
        Complaint c = new Complaint();
        c.setComplaintNo(NumberGen.complaintNo());
        c.setUserId(userId);
        c.setMerchantId(dto.getMerchantId());
        c.setOrderId(dto.getOrderId());
        c.setType(dto.getType());
        c.setContent(dto.getContent());
        c.setImages(dto.getImages() == null ? null : String.join(",", dto.getImages()));
        c.setStatus(0);
        c.setDeleted(0);
        complaintMapper.insert(c);
        return c.getId();
    }

    @Override
    public PageResult<ComplaintVO> list(Integer status, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<Complaint> q = new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getDeleted, 0)
                .orderByDesc(Complaint::getId);
        if (status != null) {
            q.eq(Complaint::getStatus, status);
        }
        IPage<Complaint> page = complaintMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public PageResult<ComplaintVO> userList(Long userId, Long pageNum, Long pageSize) {
        IPage<Complaint> page = complaintMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Complaint>()
                        .eq(Complaint::getUserId, userId)
                        .eq(Complaint::getDeleted, 0)
                        .orderByDesc(Complaint::getId));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional
    public Map<String, Object> handle(Long id, AuditDTO dto, Long adminId) {
        Complaint c = complaintMapper.selectById(id);
        if (c == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        int before = c.getStatus();
        // 契约 6.6：PROCESS→status=1(处理中)，CLOSE→status=2(已处理)
        int after = dto.getStatus() != null ? dto.getStatus()
                : ("CLOSE".equalsIgnoreCase(dto.getAction()) || "APPROVE".equalsIgnoreCase(dto.getAction())
                ? 2 : 1);
        c.setStatus(after);
        c.setHandleRemark(dto.effectiveRemark());
        c.setHandlerId(adminId);
        c.setHandleTime(LocalDateTime.now());
        complaintMapper.updateById(c);

        AuditRecord record = new AuditRecord();
        record.setBizType("COMPLAINT");
        record.setBizId(id);
        record.setAuditorId(adminId);
        record.setAction(after >= 2 ? "APPROVE" : "REJECT");
        record.setBeforeStatus(before);
        record.setAfterStatus(after);
        record.setRemark(dto.effectiveRemark());
        record.setAuditTime(LocalDateTime.now());
        record.setDeleted(0);
        auditRecordMapper.insert(record);

        // 契约 6.6 响应 data：{ complaintId, status, statusText }
        Map<String, Object> result = new HashMap<>();
        result.put("complaintId", c.getId());
        result.put("status", after);
        result.put("statusText", statusText(after));
        return result;
    }

    private ComplaintVO toVO(Complaint c) {
        ComplaintVO vo = new ComplaintVO();
        vo.setId(c.getId());
        vo.setComplaintNo(c.getComplaintNo());
        vo.setUserId(c.getUserId());
        vo.setMerchantId(c.getMerchantId());
        vo.setOrderId(c.getOrderId());
        vo.setType(c.getType());
        vo.setContent(c.getContent());
        vo.setStatus(c.getStatus());
        vo.setStatusText(statusText(c.getStatus()));
        vo.setHandleRemark(c.getHandleRemark());
        vo.setHandleTime(c.getHandleTime());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "处理中";
            case 2 -> "已处理";
            case 3 -> "已关闭";
            default -> "未知";
        };
    }
}
