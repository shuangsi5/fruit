package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 收货地址请求。 */
@Data
public class AddressDTO {

    @NotBlank(message = "收货人不能为空")
    private String receiver;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    private String province;
    private String city;
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private Integer isDefault;
}
