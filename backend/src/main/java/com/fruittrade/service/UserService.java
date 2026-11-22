package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.vo.UserVO;

public interface UserService {
    PageResult<UserVO> list(String role, Long pageNum, Long pageSize);
    void updateStatus(Long id, Integer status);
}
