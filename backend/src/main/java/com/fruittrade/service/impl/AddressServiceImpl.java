package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.domain.dto.AddressDTO;
import com.fruittrade.domain.entity.Address;
import com.fruittrade.domain.vo.AddressVO;
import com.fruittrade.mapper.AddressMapper;
import com.fruittrade.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收货地址服务实现。
 */
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<AddressVO> list(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getDeleted, 0)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getId))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> add(Long userId, AddressDTO dto) {
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            List<Address> defaults = addressMapper.selectList(
                    new LambdaQueryWrapper<Address>()
                            .eq(Address::getUserId, userId)
                            .eq(Address::getIsDefault, 1)
                            .eq(Address::getDeleted, 0));
            for (Address a : defaults) {
                a.setIsDefault(0);
                addressMapper.updateById(a);
            }
        }
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiver(dto.getReceiver());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
        address.setDeleted(0);
        addressMapper.insert(address);
        Map<String, Object> result = new HashMap<>();
        result.put("addressId", address.getId());
        return result;
    }

    @Override
    public void delete(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        address.setDeleted(1);
        addressMapper.updateById(address);
    }

    private AddressVO toVO(Address a) {
        AddressVO vo = new AddressVO();
        vo.setId(a.getId());
        vo.setReceiver(a.getReceiver());
        vo.setPhone(a.getPhone());
        vo.setProvince(a.getProvince());
        vo.setCity(a.getCity());
        vo.setDistrict(a.getDistrict());
        vo.setDetail(a.getDetail());
        vo.setIsDefault(a.getIsDefault());
        return vo;
    }
}
