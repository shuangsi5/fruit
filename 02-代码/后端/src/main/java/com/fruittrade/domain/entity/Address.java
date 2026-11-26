package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("address")
public class Address extends BaseEntity {

    private Long userId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
}
