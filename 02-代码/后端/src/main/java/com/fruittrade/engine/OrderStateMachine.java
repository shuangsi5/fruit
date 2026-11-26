package com.fruittrade.engine;

/**
 * 订单状态机：服务端强制校验状态流转，禁止跨状态跃迁。
 * 状态：0待支付 1待发货 2运输中 3已完成 4已取消 5售后中
 */
public final class OrderStateMachine {

    private OrderStateMachine() {
    }

    public static final int PENDING_PAYMENT = 0;
    public static final int PENDING_SHIPMENT = 1;
    public static final int SHIPPING = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELLED = 4;
    public static final int AFTER_SALE = 5;

    /** 判断 from -> to 是否合法 */
    public static boolean canTransition(int from, int to) {
        return switch (from) {
            case PENDING_PAYMENT -> to == PENDING_SHIPMENT || to == CANCELLED;
            case PENDING_SHIPMENT -> to == SHIPPING || to == CANCELLED;
            case SHIPPING -> to == COMPLETED || to == AFTER_SALE;
            case COMPLETED -> to == AFTER_SALE;
            case AFTER_SALE -> to == COMPLETED;
            case CANCELLED -> false;
            default -> false;
        };
    }

    /** 校验流转，非法时抛业务异常（由调用方捕获映射为 10003） */
    public static void requireTransition(int from, int to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("订单状态不允许此操作: " + from + " -> " + to);
        }
    }

    public static String statusText(int status) {
        return switch (status) {
            case PENDING_PAYMENT -> "待支付";
            case PENDING_SHIPMENT -> "待发货";
            case SHIPPING -> "运输中";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case AFTER_SALE -> "售后中";
            default -> "未知状态";
        };
    }
}
