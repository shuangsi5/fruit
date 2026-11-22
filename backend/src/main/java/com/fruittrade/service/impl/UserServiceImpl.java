package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruittrade.common.PageResult;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.domain.entity.SysUser;
import com.fruittrade.domain.vo.UserVO;
import com.fruittrade.mapper.SysUserMapper;
import com.fruittrade.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户管理服务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    public UserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<UserVO> list(String role, Long pageNum, Long pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> q =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .orderByDesc(SysUser::getId);
        if (role != null && !role.isBlank()) {
            q.eq(SysUser::getRole, role);
        }
        IPage<SysUser> page = sysUserMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    private UserVO toVO(SysUser user) {
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
}
