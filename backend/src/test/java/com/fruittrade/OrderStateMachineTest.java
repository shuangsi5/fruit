package com.fruittrade;

import com.fruittrade.engine.OrderStateMachine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 订单状态机流转合法性单测。 */
class OrderStateMachineTest {

    @Test
    void testLegalTransitions() {
        assertTrue(OrderStateMachine.canTransition(0, 1)); // 待支付 -> 待发货（支付）
        assertTrue(OrderStateMachine.canTransition(0, 4)); // 待支付 -> 已取消
        assertTrue(OrderStateMachine.canTransition(1, 2)); // 待发货 -> 运输中
        assertTrue(OrderStateMachine.canTransition(1, 4)); // 待发货 -> 已取消
        assertTrue(OrderStateMachine.canTransition(2, 3)); // 运输中 -> 已完成
        assertTrue(OrderStateMachine.canTransition(3, 5)); // 已完成 -> 售后中
        assertTrue(OrderStateMachine.canTransition(5, 3)); // 售后中 -> 已完成
    }

    @Test
    void testIllegalTransitions() {
        assertFalse(OrderStateMachine.canTransition(0, 2)); // 待支付不能直接运输中
        assertFalse(OrderStateMachine.canTransition(0, 3)); // 待支付不能直接完成
        assertFalse(OrderStateMachine.canTransition(4, 0)); // 已取消不能回退
        assertFalse(OrderStateMachine.canTransition(3, 0)); // 已完成不能回退
        assertFalse(OrderStateMachine.canTransition(2, 0));
    }

    @Test
    void testRequireTransitionThrows() {
        assertDoesNotThrow(() -> OrderStateMachine.requireTransition(0, 1));
        assertThrows(IllegalStateException.class, () -> OrderStateMachine.requireTransition(0, 3));
    }

    @Test
    void testStatusText() {
        assertEquals("待支付", OrderStateMachine.statusText(0));
        assertEquals("待发货", OrderStateMachine.statusText(1));
        assertEquals("运输中", OrderStateMachine.statusText(2));
        assertEquals("已完成", OrderStateMachine.statusText(3));
        assertEquals("已取消", OrderStateMachine.statusText(4));
        assertEquals("售后中", OrderStateMachine.statusText(5));
    }
}
