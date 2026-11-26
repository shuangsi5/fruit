package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.OrderCreateDTO;
import com.fruittrade.domain.vo.*;

public interface OrderService {
    OrderCreateVO create(Long userId, OrderCreateDTO dto);
    PayVO pay(Long userId, Long orderId, Integer payMethod);
    void cancel(Long userId, Long orderId);
    void confirm(Long userId, Long orderId);
    PageResult<OrderVO> list(Long userId, Integer status, Long pageNum, Long pageSize);
    OrderVO detail(Long orderId);
    LogisticsVO logistics(Long orderId);
    PageResult<OrderVO> merchantList(Long merchantId, Integer status, Long pageNum, Long pageSize);
    void ship(Long merchantId, Long orderId);
    void ship(Long merchantId, Long orderId, String carrier, String logisticsNo);
}
