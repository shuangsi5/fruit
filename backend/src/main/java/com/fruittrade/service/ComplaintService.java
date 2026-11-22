package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.ComplaintDTO;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.vo.ComplaintVO;

import java.util.Map;

public interface ComplaintService {
    Long create(Long userId, ComplaintDTO dto);
    PageResult<ComplaintVO> list(Integer status, Long pageNum, Long pageSize);
    PageResult<ComplaintVO> userList(Long userId, Long pageNum, Long pageSize);
    Map<String, Object> handle(Long id, AuditDTO dto, Long adminId);
}
