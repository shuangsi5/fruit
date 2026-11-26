package com.fruittrade.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务错误码枚举（对应 API 契约第 1.5 节错误码表，共 25 个码值）。
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),

    PARAM_INVALID(40001, "参数校验失败"),
    JSON_INVALID(40002, "请求体 JSON 格式错误"),
    PARAM_MISSING(40003, "缺少必填参数"),

    NOT_LOGIN(40101, "未登录或 Token 缺失"),
    TOKEN_INVALID(40102, "Token 无效或已过期"),
    LOGIN_FAILED(40103, "账号或密码错误"),

    FORBIDDEN(40301, "权限不足"),
    ACCOUNT_DISABLED(40302, "账号已被禁用"),

    NOT_FOUND(40401, "资源不存在"),
    API_NOT_FOUND(40402, "接口不存在"),
    METHOD_NOT_ALLOWED(40501, "请求方法不支持"),

    DUPLICATE(40901, "数据已存在"),
    CONCURRENCY_CONFLICT(40902, "并发冲突，请重试"),

    BIZ_FAILED(42201, "业务规则校验失败"),
    TOO_MANY_REQUESTS(42901, "请求过于频繁"),

    SYSTEM_ERROR(50001, "系统内部错误"),
    DB_ERROR(50002, "数据库异常"),
    FILE_ERROR(50003, "文件操作异常"),

    STOCK_NOT_ENOUGH(10001, "库存不足"),
    PRODUCT_OFF_SHELF(10002, "商品已下架"),
    ORDER_STATUS_INVALID(10003, "订单状态不允许此操作"),
    PAY_FAILED(10004, "支付失败"),
    AFTER_SALE_TIMEOUT(10005, "售后申请超出时限"),
    MERCHANT_NOT_APPROVED(10006, "商家未通过审核");

    private final int code;
    private final String message;
}
