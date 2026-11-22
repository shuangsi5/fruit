package com.fruittrade.controller;

import com.fruittrade.common.Result;
import com.fruittrade.domain.dto.AddressDTO;
import com.fruittrade.domain.vo.AddressVO;
import com.fruittrade.security.SecurityUtils;
import com.fruittrade.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收货地址接口。
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Result<List<AddressVO>> list() {
        return Result.ok(addressService.list(SecurityUtils.getUserId()));
    }

    @PostMapping
    public Result<Map<String, Object>> add(@Valid @RequestBody AddressDTO dto) {
        return Result.ok(addressService.add(SecurityUtils.getUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(SecurityUtils.getUserId(), id);
        return Result.ok();
    }
}
