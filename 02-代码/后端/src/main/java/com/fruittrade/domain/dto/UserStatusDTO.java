package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 用户状态变更请求。 */
@Data
public class UserStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
