package com.fruittrade.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 订单 VO（列表与详情通用）。 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private String merchantName;
    private String receiver;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal payAmount;
    private Integer payMethod;
    private Integer status;
    private String statusText;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private String remark;
    private List<OrderItemVO> items;
    private LogisticsVO logistics;
}
