package com.fruittrade.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 投诉申请请求。 */
@Data
public class ComplaintDTO {

    private Long merchantId;
    private Long orderId;

    @NotNull(message = "投诉类型不能为空")
    private Integer type;

    private String content;
    private List<String> images;
}
