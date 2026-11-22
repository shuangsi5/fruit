package com.fruittrade.service;

import com.fruittrade.domain.dto.AddressDTO;
import com.fruittrade.domain.vo.AddressVO;
import java.util.List;
import java.util.Map;

public interface AddressService {
    List<AddressVO> list(Long userId);
    Map<String, Object> add(Long userId, AddressDTO dto);
    void delete(Long userId, Long id);
}
