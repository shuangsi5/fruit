package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.vo.MerchantVO;

public interface MerchantService {
    PageResult<MerchantVO> list(Integer auditStatus, Long pageNum, Long pageSize);
    void audit(Long id, AuditDTO dto, Long adminId);
    MerchantVO getByUserId(Long userId);
}
