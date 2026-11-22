package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 投诉表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("complaint")
public class Complaint extends BaseEntity {

    private String complaintNo;
    private Long userId;
    private Long merchantId;
    private Long orderId;
    private Integer type;
    private String content;
    private String images;
    private Integer status;
    private String handleRemark;
    private Long handlerId;
    private LocalDateTime handleTime;
}
