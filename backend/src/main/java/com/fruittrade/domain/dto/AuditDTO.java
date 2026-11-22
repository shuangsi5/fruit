package com.fruittrade.domain.dto;

import lombok.Data;

/**
 * 审核/处理请求（商家审核、售后处理、投诉处理通用）。
 * 兼容两套字段：数字状态码 status + remark，以及动作码 action + handleRemark。
 */
@Data
public class AuditDTO {

    /** 数字状态码（status 与 action 至少提供其一） */
    private Integer status;

    private String remark;

    /** 动作码：APPROVE / REJECT / PROCESS / CLOSE */
    private String action;

    /** 处理意见（等价 remark） */
    private String handleRemark;

    /** 取备注，兼容 remark 与 handleRemark */
    public String effectiveRemark() {
        return remark != null ? remark : handleRemark;
    }
}
