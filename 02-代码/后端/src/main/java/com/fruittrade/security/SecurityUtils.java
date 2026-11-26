package com.fruittrade.security;

import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.RoleConstant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类：获取当前登录主体、用户ID、商家ID，并做角色/数据权限校验。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return loginUser;
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getRole() {
        return getLoginUser().getRole();
    }

    public static Long getMerchantId() {
        LoginUser user = getLoginUser();
        if (RoleConstant.ADMIN.equals(user.getRole())) {
            return null;
        }
        return user.getMerchantId();
    }

    /** 校验数据归属：本人/本商家/管理员可访问 */
    public static boolean canAccess(Long ownerUserId, Long ownerMerchantId) {
        LoginUser user = getLoginUser();
        if (RoleConstant.ADMIN.equals(user.getRole())) {
            return true;
        }
        if (RoleConstant.MERCHANT.equals(user.getRole())) {
            return user.getMerchantId() != null && user.getMerchantId().equals(ownerMerchantId);
        }
        return user.getUserId() != null && user.getUserId().equals(ownerUserId);
    }

    public static void requireOwner(Long ownerUserId, Long ownerMerchantId) {
        if (!canAccess(ownerUserId, ownerMerchantId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
