package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.OrderCreateDTO;
import com.fruittrade.domain.dto.OrderItemDTO;
import com.fruittrade.domain.entity.*;
import com.fruittrade.domain.vo.*;
import com.fruittrade.engine.OrderStateMachine;
import com.fruittrade.mapper.*;
import com.fruittrade.service.NumberGen;
import com.fruittrade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现：下单、支付、取消、确认、物流、商家发货。
 * 核心：服务端重算金额、库存预占/扣减/回补、乐观锁防超卖、状态机强校验。
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final AddressMapper addressMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final LogisticsMapper logisticsMapper;
    private final MerchantMapper merchantMapper;
    private final ProductImageMapper productImageMapper;
    private final ObjectMapper objectMapper;

    @Value("${business.order-timeout-minutes:30}")
    private int timeoutMinutes;

    public OrderServiceImpl(OrdersMapper ordersMapper, OrderItemMapper orderItemMapper,
                            ProductMapper productMapper, InventoryMapper inventoryMapper,
                            InventoryLogMapper inventoryLogMapper, AddressMapper addressMapper,
                            PaymentRecordMapper paymentRecordMapper, LogisticsMapper logisticsMapper,
                            MerchantMapper merchantMapper, ProductImageMapper productImageMapper,
                            ObjectMapper objectMapper) {
        this.ordersMapper = ordersMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.addressMapper = addressMapper;
        this.paymentRecordMapper = paymentRecordMapper;
       this.logisticsMapper = logisticsMapper;
        this.merchantMapper = merchantMapper;
        this.productImageMapper = productImageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public OrderCreateVO create(Long userId, OrderCreateDTO dto) {
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        }

        // 服务端按 price * qty 重算金额，忽略前端任何金额字段
        BigDecimal total = BigDecimal.ZERO;
        List<Product> products = new ArrayList<>();
        Long merchantId = null;
        for (OrderItemDTO item : dto.getItems()) {
            Product p = productMapper.selectById(item.getProductId());
            if (p == null || !Integer.valueOf(1).equals(p.getStatus())) {
                throw new BusinessException(ErrorCode.PRODUCT_OFF_SHELF, "商品不存在或已下架: " + item.getProductId());
            }
            Inventory inv = getInventory(p.getId());
            if (inv == null || inv.getStock() == null || inv.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "库存不足: " + p.getName());
            }
            if (merchantId == null) {
                merchantId = p.getMerchantId();
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            products.add(p);
        }

        Orders order = new Orders();
        order.setOrderNo(NumberGen.orderNo());
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setAddressId(address.getId());
        order.setReceiver(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(joinAddress(address));
        order.setTotalAmount(total);
        order.setFreight(BigDecimal.ZERO);
        order.setPayAmount(total);
        order.setPayMethod(dto.getPayMethod() == null ? 1 : dto.getPayMethod());
        order.setStatus(OrderStateMachine.PENDING_PAYMENT);
        order.setRemark(dto.getRemark());
        order.setDeleted(0);
        ordersMapper.insert(order);

        // 生成明细 + 锁库存（乐观锁 CAS）
        for (int i = 0; i < dto.getItems().size(); i++) {
            OrderItemDTO item = dto.getItems().get(i);
            Product p = products.get(i);
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductImage(mainImage(p.getId()));
            oi.setPrice(p.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            oi.setDeleted(0);
            orderItemMapper.insert(oi);
            lockStock(p.getId(), merchantId, item.getQuantity(), order.getId(), userId);
        }

        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setExpireTime(LocalDateTime.now().plusMinutes(timeoutMinutes));
        return vo;
    }

    @Override
    @Transactional
    public PayVO pay(Long userId, Long orderId, Integer payMethod) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "订单不存在或非本人订单");
        }
        if (order.getStatus() != OrderStateMachine.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Integer method = payMethod != null ? payMethod : order.getPayMethod();
        if (method == null) {
            method = 1;
        }

        // 模拟支付直接成功，记录支付流水
        PaymentRecord pr = new PaymentRecord();
        pr.setPaymentNo(NumberGen.paymentNo());
        pr.setOrderId(order.getId());
        pr.setOrderNo(order.getOrderNo());
        pr.setUserId(userId);
        pr.setAmount(order.getPayAmount());
        pr.setPayMethod(method);
        pr.setStatus(1);
        pr.setTradeNo("SIM-" + System.nanoTime());
        pr.setPaidTime(LocalDateTime.now());
        pr.setDeleted(0);
        paymentRecordMapper.insert(pr);

        // 支付成功：扣减 locked_stock
        List<OrderItem> items = orderItems(orderId);
        for (OrderItem oi : items) {
            deductLockedStock(oi.getProductId(), oi.getQuantity(), orderId, userId);
        }

        // 状态 0 -> 1，生成待发货物流
        order.setStatus(OrderStateMachine.PENDING_SHIPMENT);
        order.setPayMethod(method);
        order.setPayTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        Logistics logistics = new Logistics();
        logistics.setOrderId(order.getId());
        logistics.setLogisticsNo(NumberGen.logisticsNo());
        logistics.setCarrier("顺丰速运");
        logistics.setStatus(0);
        logistics.setTraces("[]");
        logistics.setDeleted(0);
        logisticsMapper.insert(logistics);

        PayVO vo = new PayVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPaymentNo(pr.getPaymentNo());
        vo.setAmount(pr.getAmount());
        vo.setStatus(1);
        vo.setPayTime(pr.getPaidTime());
        log.info("订单支付成功 orderNo={} amount={}", order.getOrderNo(), order.getPayAmount());
        return vo;
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // 仅待支付可用户取消；待发货取消需商家同意（此处允许 0/1 -> 4）
        if (order.getStatus() != OrderStateMachine.PENDING_PAYMENT
                && order.getStatus() != OrderStateMachine.PENDING_SHIPMENT) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        int from = order.getStatus();
        OrderStateMachine.requireTransition(from, OrderStateMachine.CANCELLED);
        // 回补库存（释放锁定）
        if (from == OrderStateMachine.PENDING_PAYMENT) {
            for (OrderItem oi : orderItems(orderId)) {
                releaseStock(oi.getProductId(), oi.getQuantity(), orderId, userId);
            }
        }
        order.setStatus(OrderStateMachine.CANCELLED);
        order.setCloseTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional
    public void confirm(Long userId, Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        OrderStateMachine.requireTransition(order.getStatus(), OrderStateMachine.COMPLETED);
        order.setStatus(OrderStateMachine.COMPLETED);
        order.setFinishTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        updateLogisticsOnDeliver(orderId);
    }

    @Override
    public PageResult<OrderVO> list(Long userId, Integer status, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<Orders> q = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getDeleted, 0)
                .orderByDesc(Orders::getId);
        if (status != null) {
            q.eq(Orders::getStatus, status);
        }
        IPage<Orders> page = ordersMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public OrderVO detail(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return toVO(order);
    }

    @Override
    public LogisticsVO logistics(Long orderId) {
        Logistics l = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>()
                        .eq(Logistics::getOrderId, orderId)
                        .eq(Logistics::getDeleted, 0)
                        .last("limit 1"));
        if (l == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "物流信息不存在");
        }
        return toLogisticsVO(l);
    }

    @Override
    public PageResult<OrderVO> merchantList(Long merchantId, Integer status, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<Orders> q = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId)
                .eq(Orders::getDeleted, 0)
                .orderByDesc(Orders::getId);
        if (status != null) {
            q.eq(Orders::getStatus, status);
        }
        IPage<Orders> page = ordersMapper.selectPage(new Page<>(pageNum, pageSize), q);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional
    public void ship(Long merchantId, Long orderId) {
        ship(merchantId, orderId, null, null);
    }

    @Override
    @Transactional
    public void ship(Long merchantId, Long orderId, String carrier, String logisticsNo) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        OrderStateMachine.requireTransition(order.getStatus(), OrderStateMachine.SHIPPING);
        order.setStatus(OrderStateMachine.SHIPPING);
        order.setShipTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        Logistics l = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>()
                        .eq(Logistics::getOrderId, orderId)
                        .eq(Logistics::getDeleted, 0)
                        .last("limit 1"));
        if (l == null) {
            l = new Logistics();
            l.setOrderId(orderId);
            l.setLogisticsNo(NumberGen.logisticsNo());
            l.setCarrier("顺丰速运");
            l.setDeleted(0);
        }
        // 前端可指定承运商与运单号，缺省则沿用默认值
        if (carrier != null && !carrier.isBlank()) {
            l.setCarrier(carrier);
        }
        if (logisticsNo != null && !logisticsNo.isBlank()) {
            l.setLogisticsNo(logisticsNo);
        }
        l.setStatus(2);
        l.setShipTime(LocalDateTime.now());
        List<TraceVO> traces = parseTraces(l.getTraces());
        traces.add(new TraceVO(fmt(LocalDateTime.now()), "快件已发出，运输中"));
        l.setTraces(toJson(traces));
        if (l.getId() == null) {
            logisticsMapper.insert(l);
        } else {
            logisticsMapper.updateById(l);
        }
    }

    // ---------- 库存操作（乐观锁 CAS 防超卖） ----------

    private Inventory getInventory(Long productId) {
        return inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, productId)
                        .eq(Inventory::getDeleted, 0)
                        .last("limit 1"));
    }

    private void lockStock(Long productId, Long merchantId, int qty, Long orderId, Long operatorId) {
        Inventory inv = getInventory(productId);
        if (inv == null || inv.getStock() < qty) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        int before = inv.getStock();
        LambdaUpdateWrapper<Inventory> uw = new LambdaUpdateWrapper<Inventory>()
                .eq(Inventory::getId, inv.getId())
                .eq(Inventory::getVersion, inv.getVersion())
                .set(Inventory::getStock, before - qty)
                .set(Inventory::getLockedStock, (inv.getLockedStock() == null ? 0 : inv.getLockedStock()) + qty)
                .set(Inventory::getVersion, inv.getVersion() + 1);
        if (inventoryMapper.update(null, uw) == 0) {
            throw new BusinessException(ErrorCode.CONCURRENCY_CONFLICT);
        }
        writeInventoryLog(productId, merchantId, 5, -qty, before, before - qty,
                "ORDER", orderId, operatorId, "下单锁定库存");
    }

    private void deductLockedStock(Long productId, int qty, Long orderId, Long operatorId) {
        Inventory inv = getInventory(productId);
        if (inv == null || inv.getLockedStock() == null || inv.getLockedStock() < qty) {
            throw new BusinessException(ErrorCode.CONCURRENCY_CONFLICT, "锁定库存异常");
        }
        int before = inv.getLockedStock();
        LambdaUpdateWrapper<Inventory> uw = new LambdaUpdateWrapper<Inventory>()
                .eq(Inventory::getId, inv.getId())
                .eq(Inventory::getVersion, inv.getVersion())
                .set(Inventory::getLockedStock, before - qty)
                .set(Inventory::getVersion, inv.getVersion() + 1);
        if (inventoryMapper.update(null, uw) == 0) {
            throw new BusinessException(ErrorCode.CONCURRENCY_CONFLICT);
        }
        writeInventoryLog(productId, inv.getMerchantId(), 2, -qty, inv.getStock(), inv.getStock(),
                "ORDER", orderId, operatorId, "支付成功扣减锁定库存");
    }

    private void releaseStock(Long productId, int qty, Long orderId, Long operatorId) {
        Inventory inv = getInventory(productId);
        if (inv == null) {
            return;
        }
        int before = inv.getStock();
        LambdaUpdateWrapper<Inventory> uw = new LambdaUpdateWrapper<Inventory>()
                .eq(Inventory::getId, inv.getId())
                .eq(Inventory::getVersion, inv.getVersion())
                .set(Inventory::getStock, before + qty)
                .set(Inventory::getLockedStock, Math.max(0, (inv.getLockedStock() == null ? 0 : inv.getLockedStock()) - qty))
                .set(Inventory::getVersion, inv.getVersion() + 1);
        if (inventoryMapper.update(null, uw) == 0) {
            throw new BusinessException(ErrorCode.CONCURRENCY_CONFLICT);
        }
        writeInventoryLog(productId, inv.getMerchantId(), 6, qty, before, before + qty,
                "ORDER", orderId, operatorId, "取消订单回补库存");
    }

    private void writeInventoryLog(Long productId, Long merchantId, int changeType, int qty,
                                   int before, int after, String refType, Long refId,
                                   Long operatorId, String remark) {
        InventoryLog logEntity = new InventoryLog();
        logEntity.setProductId(productId);
        logEntity.setMerchantId(merchantId);
        logEntity.setChangeType(changeType);
        logEntity.setQuantity(qty);
        logEntity.setBeforeStock(before);
        logEntity.setAfterStock(after);
        logEntity.setRefType(refType);
        logEntity.setRefId(refId);
        logEntity.setOperatorId(operatorId);
        logEntity.setRemark(remark);
        logEntity.setCreateTime(LocalDateTime.now());
        inventoryLogMapper.insert(logEntity);
    }

    // ---------- 组装 VO ----------

    private List<OrderItem> orderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .eq(OrderItem::getDeleted, 0));
    }

    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setMerchantId(order.getMerchantId());
        Merchant m = merchantMapper.selectById(order.getMerchantId());
        vo.setMerchantName(m == null ? null : m.getMerchantName());
        vo.setReceiver(order.getReceiver());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreight(order.getFreight());
        vo.setPayAmount(order.getPayAmount());
        vo.setPayMethod(order.getPayMethod());
        vo.setStatus(order.getStatus());
        vo.setStatusText(OrderStateMachine.statusText(order.getStatus()));
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setRemark(order.getRemark());
        vo.setItems(orderItems(order.getId()).stream().map(this::toItemVO).collect(Collectors.toList()));
        Logistics l = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>()
                        .eq(Logistics::getOrderId, order.getId())
                        .eq(Logistics::getDeleted, 0)
                        .last("limit 1"));
        if (l != null) {
            vo.setLogistics(toLogisticsVO(l));
        }
        return vo;
    }

    private OrderItemVO toItemVO(OrderItem oi) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(oi.getId());
        vo.setProductId(oi.getProductId());
        vo.setProductName(oi.getProductName());
        vo.setProductImage(oi.getProductImage());
        vo.setPrice(oi.getPrice());
        vo.setQuantity(oi.getQuantity());
        vo.setSubtotal(oi.getSubtotal());
        return vo;
    }

    private LogisticsVO toLogisticsVO(Logistics l) {
        LogisticsVO vo = new LogisticsVO();
        vo.setId(l.getId());
        vo.setLogisticsNo(l.getLogisticsNo());
        vo.setCarrier(l.getCarrier());
        vo.setStatus(l.getStatus());
        vo.setStatusText(switch (l.getStatus() == null ? 0 : l.getStatus()) {
            case 0 -> "待发货";
            case 1 -> "已揽收";
            case 2 -> "运输中";
            case 3 -> "派送中";
            case 4 -> "已签收";
            default -> "未知";
        });
        vo.setTraces(parseTraces(l.getTraces()));
        vo.setShipTime(l.getShipTime());
        vo.setDeliverTime(l.getDeliverTime());
        return vo;
    }

    private void updateLogisticsOnDeliver(Long orderId) {
        Logistics l = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>()
                        .eq(Logistics::getOrderId, orderId)
                        .eq(Logistics::getDeleted, 0)
                        .last("limit 1"));
        if (l == null) {
            return;
        }
        l.setStatus(4);
        l.setDeliverTime(LocalDateTime.now());
        List<TraceVO> traces = parseTraces(l.getTraces());
        traces.add(new TraceVO(fmt(LocalDateTime.now()), "快件已签收"));
        l.setTraces(toJson(traces));
        logisticsMapper.updateById(l);
    }

    private List<TraceVO> parseTraces(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<TraceVO>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String toJson(List<TraceVO> traces) {
        try {
            return objectMapper.writeValueAsString(traces);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String mainImage(Long productId) {
        ProductImage img = productImageMapper.selectOne(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .eq(ProductImage::getIsMain, 1)
                        .eq(ProductImage::getDeleted, 0)
                        .last("limit 1"));
        return img == null ? null : img.getUrl();
    }

    private String joinAddress(Address a) {
        return (a.getProvince() == null ? "" : a.getProvince())
                + (a.getCity() == null ? "" : a.getCity())
                + (a.getDistrict() == null ? "" : a.getDistrict())
                + (a.getDetail() == null ? "" : a.getDetail());
    }

    private String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
