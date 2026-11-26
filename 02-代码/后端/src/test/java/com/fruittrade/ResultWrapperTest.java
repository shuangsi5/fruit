package com.fruittrade;

import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.common.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 统一响应与分页包装单测。 */
class ResultWrapperTest {

    @Test
    void testSuccessResult() {
        Result<String> r = Result.ok("hello");
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("hello", r.getData());
        assertTrue(r.isSuccess());
        assertTrue(r.getTimestamp() > 0);
    }

    @Test
    void testFailResult() {
        Result<Void> r = Result.fail(ErrorCode.STOCK_NOT_ENOUGH);
        assertEquals(10001, r.getCode());
        assertEquals("库存不足", r.getMessage());
        assertFalse(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void testPageResult() {
        PageResult<String> page = PageResult.of(List.of("a", "b"), 25L, 2L, 10L);
        assertEquals(3, page.getPages());
        assertEquals(2, page.getList().size());
        assertEquals(25, page.getTotal());
        assertEquals(2, page.getPageNum());
        assertEquals(10, page.getPageSize());
    }

    @Test
    void testAllErrorCodesPresent() {
        assertEquals(25, ErrorCode.values().length);
        assertEquals(0, ErrorCode.SUCCESS.getCode());
        assertEquals(40102, ErrorCode.TOKEN_INVALID.getCode());
        assertEquals(10003, ErrorCode.ORDER_STATUS_INVALID.getCode());
        assertEquals(10006, ErrorCode.MERCHANT_NOT_APPROVED.getCode());
    }
}
