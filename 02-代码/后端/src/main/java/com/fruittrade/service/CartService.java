package com.fruittrade.service;

import com.fruittrade.domain.dto.CartAddDTO;
import com.fruittrade.domain.dto.CartUpdateDTO;
import com.fruittrade.domain.vo.CartVO;
import java.util.Map;

public interface CartService {
    CartVO getCart(Long userId);
    Map<String, Object> add(Long userId, CartAddDTO dto);
    Map<String, Object> update(Long userId, Long cartId, CartUpdateDTO dto);
    void delete(Long userId, Long cartId);
}
