package com.fruittrade.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 投诉 VO。 */
@Data
public class ComplaintVO {

    private Long id;
    private String complaintNo;
    private Long userId;
    private Long merchantId;
    private Long orderId;
    private Integer type;
    private String content;
    private Integer status;
    private String statusText;
    private String handleRemark;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}
