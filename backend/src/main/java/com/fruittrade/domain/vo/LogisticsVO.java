package com.fruittrade.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 物流 VO。 */
@Data
public class LogisticsVO {

    private Long id;
    private String logisticsNo;
    private String carrier;
    private Integer status;
    private String statusText;
    private List<TraceVO> traces;
    private LocalDateTime shipTime;
    private LocalDateTime deliverTime;
}
