package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.domain.dto.CartAddDTO;
import com.fruittrade.domain.dto.CartUpdateDTO;
import com.fruittrade.domain.entity.Cart;
import com.fruittrade.domain.entity.Inventory;
import com.fruittrade.domain.entity.Product;
import com.fruittrade.domain.entity.ProductImage;
import com.fruittrade.domain.vo.CartItemVO;
import com.fruittrade.domain.vo.CartVO;
import com.fruittrade.mapper.CartMapper;
import com.fruittrade.mapper.InventoryMapper;
import com.fruittrade.mapper.ProductImageMapper;
import com.fruittrade.mapper.ProductMapper;
import com.fruittrade.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现。价格以商品实时价为准。
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final ProductImageMapper productImageMapper;

    public CartServiceImpl(CartMapper cartMapper, ProductMapper productMapper, InventoryMapper inventoryMapper,
                           ProductImageMapper productImageMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.productImageMapper = productImageMapper;
    }

    @Override
    public CartVO getCart(Long userId) {
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getDeleted, 0)
                        .orderByDesc(Cart::getId));
        List<CartItemVO> items = carts.stream().map(cart -> {
            Product p = productMapper.selectById(cart.getProductId());
            CartItemVO vo = new CartItemVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected());
            if (p != null) {
                vo.setProductName(p.getName());
                vo.setMainImage(mainImage(p.getId()));
                BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
                vo.setPrice(price);
                vo.setSubtotal(price.multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
            Inventory inv = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getProductId, cart.getProductId())
                            .eq(Inventory::getDeleted, 0)
                            .last("limit 1"));
            vo.setStock(inv == null ? 0 : inv.getStock());
            return vo;
        }).collect(Collectors.toList());

        CartVO result = new CartVO();
        result.setItems(items);
        result.setTotalQuantity(items.stream().mapToInt(i -> i.getQuantity() == null ? 0 : i.getQuantity()).sum());
        result.setSelectedSubtotal(items.stream()
                .filter(i -> i.getSelected() != null && i.getSelected() == 1)
                .map(i -> i.getSubtotal() == null ? BigDecimal.ZERO : i.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> add(Long userId, CartAddDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SHELF);
        }
        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, dto.getProductId())
                        .eq(Inventory::getDeleted, 0)
                        .last("limit 1"));
        if (inv == null || inv.getStock() < dto.getQuantity()) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        Cart exist = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, dto.getProductId())
                        .eq(Cart::getDeleted, 0)
                        .last("limit 1"));
        Cart cart;
        if (exist != null) {
            int newQty = exist.getQuantity() + dto.getQuantity();
            if (newQty > inv.getStock()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            exist.setQuantity(newQty);
            cart = exist;
        } else {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setQuantity(dto.getQuantity());
            cart.setSelected(1);
            cart.setDeleted(0);
        }
        if (exist != null) {
            cartMapper.updateById(cart);
        } else {
            cartMapper.insert(cart);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("cartId", cart.getId());
        result.put("quantity", cart.getQuantity());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> update(Long userId, Long cartId, CartUpdateDTO dto) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, cart.getProductId())
                        .eq(Inventory::getDeleted, 0)
                        .last("limit 1"));
        if (inv == null || inv.getStock() < dto.getQuantity()) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        cart.setQuantity(dto.getQuantity());
        if (dto.getSelected() != null) {
            cart.setSelected(dto.getSelected());
        }
        cartMapper.updateById(cart);
        Product p = productMapper.selectById(cart.getProductId());
        BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        Map<String, Object> result = new HashMap<>();
        result.put("cartId", cart.getId());
        result.put("quantity", cart.getQuantity());
        result.put("subtotal", subtotal);
        return result;
    }

    @Override
    public void delete(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        cart.setDeleted(1);
        cartMapper.updateById(cart);
    }

    /** 取主图 */
    private String mainImage(Long productId) {
        ProductImage image = productImageMapper.selectOne(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .eq(ProductImage::getIsMain, 1)
                        .eq(ProductImage::getDeleted, 0)
                        .last("limit 1"));
        if (image == null) {
            image = productImageMapper.selectOne(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, productId)
                            .eq(ProductImage::getDeleted, 0)
                            .orderByAsc(ProductImage::getSort)
                            .last("limit 1"));
        }
        return image == null ? null : image.getUrl();
    }
}
