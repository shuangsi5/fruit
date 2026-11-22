package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruittrade.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审核记录表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_record")
public class AuditRecord extends BaseEntity {

    private String bizType;
    private Long bizId;
    private Long auditorId;
    private String action;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String remark;
    private LocalDateTime auditTime;
}
