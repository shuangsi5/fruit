package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商家表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private Long userId;
    private String merchantName;
    private String licenseNo;
    private String licenseImage;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
    private Integer status;
}
