package com.fruittrade.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.domain.entity.SysUser;
import com.fruittrade.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户详情加载：根据登录账号查询系统用户。
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    public CustomUserDetailsService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
                        .last("limit 1"));
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRole(user.getRole());
        loginUser.setMerchantId(user.getMerchantId());
        loginUser.setStatus(user.getStatus());
        loginUser.setPassword(user.getPassword());
        return loginUser;
    }
}
