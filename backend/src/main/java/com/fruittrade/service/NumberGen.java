package com.fruittrade.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/** 业务单号生成器（订单号、支付号等）。 */
public final class NumberGen {

    private static final AtomicLong SEQ = new AtomicLong(0);

    private NumberGen() {
    }

    /** 订单号：yyyyMMdd + 4位序列 + 4位自增，如 2026112100010001 */
    public static String orderNo() {
        return gen("");
    }

    public static String paymentNo() {
        return "PAY" + gen("");
    }

    public static String afterSaleNo() {
        return "AS" + gen("");
    }

    public static String complaintNo() {
        return "CP" + gen("");
    }

    public static String logisticsNo() {
        return "SF" + String.valueOf(System.nanoTime()).substring(6);
    }

    private static String gen(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long v = SEQ.incrementAndGet() % 10000;
        return prefix + date + String.format("%04d", v);
    }
}
