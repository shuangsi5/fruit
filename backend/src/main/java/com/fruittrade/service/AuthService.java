package com.fruittrade.service;

import com.fruittrade.domain.dto.LoginDTO;
import com.fruittrade.domain.dto.RegisterDTO;
import com.fruittrade.domain.vo.UserVO;

public interface AuthService {
    UserVO login(LoginDTO dto);
    void register(RegisterDTO dto);
    UserVO currentUser();
    void logout();
}
