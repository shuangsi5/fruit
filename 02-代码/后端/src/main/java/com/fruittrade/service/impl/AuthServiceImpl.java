package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.RoleConstant;
import com.fruittrade.domain.dto.LoginDTO;
import com.fruittrade.domain.dto.RegisterDTO;
import com.fruittrade.domain.entity.SysUser;
import com.fruittrade.domain.vo.UserVO;
import com.fruittrade.mapper.SysUserMapper;
import com.fruittrade.security.JwtUtil;
import com.fruittrade.security.LoginUser;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .eq(SysUser::getDeleted, 0)
                        .last("limit 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getMerchantId());
        UserVO vo = new UserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setMerchantId(user.getMerchantId());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setToken(token);
        return vo;
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        Long exists = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .eq(SysUser::getDeleted, 0));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE, "账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(RoleConstant.USER);
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);
    }

    @Override
    public UserVO currentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setMerchantId(user.getMerchantId());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        return vo;
    }

    @Override
    public void logout() {
        // 无状态 JWT：由前端清除本地 Token，服务端无操作。
    }
}
