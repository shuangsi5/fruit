package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.AfterSaleDTO;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.vo.AfterSaleVO;

public interface AfterSaleService {
    Long apply(Long userId, AfterSaleDTO dto);
    PageResult<AfterSaleVO> userList(Long userId, Long pageNum, Long pageSize);
    PageResult<AfterSaleVO> merchantList(Long merchantId, Long pageNum, Long pageSize);
    void handle(Long merchantId, Long id, AuditDTO dto, Long handlerId);
}
