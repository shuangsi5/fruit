package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.entity.AuditRecord;
import com.fruittrade.domain.entity.Merchant;
import com.fruittrade.domain.vo.MerchantVO;
import com.fruittrade.mapper.AuditRecordMapper;
import com.fruittrade.mapper.MerchantMapper;
import com.fruittrade.service.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 商家管理服务实现（审核、列表）。
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final AuditRecordMapper auditRecordMapper;

    public MerchantServiceImpl(MerchantMapper merchantMapper, AuditRecordMapper auditRecordMapper) {
        this.merchantMapper = merchantMapper;
        this.auditRecordMapper = auditRecordMapper;
    }

    @Override
    public PageResult<MerchantVO> list(Integer auditStatus, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<Merchant> q = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getDeleted, 0)
                .orderByDesc(Merchant::getId);
        if (auditStatus != null) {
            q.eq(Merchant::getAuditStatus, auditStatus);
        }
        IPage<Merchant> page = merchantMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional
    public void audit(Long id, AuditDTO dto, Long adminId) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商家不存在");
        }
        int before = merchant.getAuditStatus();
        int after = dto.getStatus() != null ? dto.getStatus()
                : ("REJECT".equalsIgnoreCase(dto.getAction()) ? 2 : 1);
        if (after != 1 && after != 2) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "审核结果须为 1 通过或 2 驳回");
        }
        merchant.setAuditStatus(after);
        merchant.setAuditRemark(dto.effectiveRemark());
        merchant.setAuditTime(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        AuditRecord record = new AuditRecord();
        record.setBizType("MERCHANT");
        record.setBizId(id);
        record.setAuditorId(adminId);
        record.setAction(after == 1 ? "APPROVE" : "REJECT");
        record.setBeforeStatus(before);
        record.setAfterStatus(after);
        record.setRemark(dto.effectiveRemark());
        record.setAuditTime(LocalDateTime.now());
        record.setDeleted(0);
        auditRecordMapper.insert(record);
    }

    @Override
    public MerchantVO getByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, userId)
                        .eq(Merchant::getDeleted, 0)
                        .last("limit 1"));
        return merchant == null ? null : toVO(merchant);
    }

    private MerchantVO toVO(Merchant m) {
        MerchantVO vo = new MerchantVO();
        vo.setId(m.getId());
        vo.setUserId(m.getUserId());
        vo.setMerchantName(m.getMerchantName());
        vo.setLicenseNo(m.getLicenseNo());
        vo.setContactPerson(m.getContactPerson());
        vo.setContactPhone(m.getContactPhone());
        vo.setAddress(m.getAddress());
        vo.setAuditStatus(m.getAuditStatus());
        vo.setAuditStatusText(m.getAuditStatus() == null ? "" :
                switch (m.getAuditStatus()) {
                    case 0 -> "待审核";
                    case 1 -> "审核通过";
                    case 2 -> "审核驳回";
                    default -> "未知";
                });
        vo.setAuditRemark(m.getAuditRemark());
        vo.setStatus(m.getStatus());
        return vo;
    }
}
