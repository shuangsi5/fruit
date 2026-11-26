package com.fruittrade.domain.vo;

import lombok.Data;

/** 登录 / 当前用户信息 VO。 */
@Data
public class UserVO {

    private Long userId;
    private String username;
    private String role;
    private Long merchantId;
    private String realName;
    private String avatar;
    private Integer status;
    private String token;
}
