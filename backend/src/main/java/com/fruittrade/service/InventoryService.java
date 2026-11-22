package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.InventoryUpdateDTO;
import com.fruittrade.domain.vo.InventoryVO;
import com.fruittrade.domain.vo.WarningVO;
import java.util.List;

public interface InventoryService {
    PageResult<InventoryVO> list(Long merchantId, Long pageNum, Long pageSize);
    void update(Long merchantId, Long productId, InventoryUpdateDTO dto);
    List<WarningVO> warnings(Long merchantId);
}
