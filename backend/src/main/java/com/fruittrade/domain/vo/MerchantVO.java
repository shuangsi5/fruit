package com.fruittrade.domain.vo;

import lombok.Data;

/** 商家 VO。 */
@Data
public class MerchantVO {

    private Long id;
    private Long userId;
    private String merchantName;
    private String licenseNo;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer auditStatus;
    private String auditStatusText;
    private String auditRemark;
    private Integer status;
}
