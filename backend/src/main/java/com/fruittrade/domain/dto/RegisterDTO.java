package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 注册请求。 */
@Data
public class RegisterDTO {

    @NotBlank(message = "账号不能为空")
    @Size(max = 64, message = "账号过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String realName;
    private String phone;
    private String email;
}
