package com.fruittrade.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐记录表实体（日志表，仅保留 create_time，不继承 BaseEntity）。
 */
@Data
@TableName("recommendation_log")
public class RecommendationLog {

    private Long id;
    private Long userId;
    private Long productId;
    private BigDecimal score;
    private String source;
    private String algorithmVersion;
    private String scene;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
