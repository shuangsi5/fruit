package com.fruittrade.controller;

import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.CartAddDTO;
import com.fruittrade.domain.dto.CartUpdateDTO;
import com.fruittrade.domain.vo.CartVO;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 购物车接口。
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<CartVO> list() {
        return Result.ok(cartService.getCart(SecurityUtils.getUserId()));
    }

    @PostMapping
    public Result<Map<String, Object>> add(@Valid @RequestBody CartAddDTO dto) {
        return Result.ok(cartService.add(SecurityUtils.getUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id,
                                              @Valid @RequestBody CartUpdateDTO dto) {
        return Result.ok(cartService.update(SecurityUtils.getUserId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(SecurityUtils.getUserId(), id);
        return Result.ok();
    }
}
