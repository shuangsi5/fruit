package com.fruittrade.domain.vo;

import lombok.Data;

/** 收货地址 VO。 */
@Data
public class AddressVO {

    private Long id;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
}
