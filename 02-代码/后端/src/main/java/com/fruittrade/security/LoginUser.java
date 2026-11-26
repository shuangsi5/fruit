package com.fruittrade.security;

import com.fruittrade.common.RoleConstant;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录主体，实现 Spring Security 的 UserDetails。
 */
@Data
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String role;
    private Long merchantId;
    private Integer status;
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = switch (role == null ? RoleConstant.USER : role) {
            case RoleConstant.ADMIN -> "ROLE_ADMIN";
            case RoleConstant.MERCHANT -> "ROLE_MERCHANT";
            default -> "ROLE_USER";
        };
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == null || status == 1;
    }
}
