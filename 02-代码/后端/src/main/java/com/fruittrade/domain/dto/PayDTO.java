package com.fruittrade.domain.dto;

import lombok.Data;

/** 支付请求。payMethod：1 模拟微信，2 模拟支付宝。 */
@Data
public class PayDTO {

    private Integer payMethod;
}
