package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruittrade.common.BusinessException;
import com.fruittrade.common.ErrorCode;
import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.AfterSaleDTO;
import com.fruittrade.domain.dto.AuditDTO;
import com.fruittrade.domain.entity.AfterSale;
import com.fruittrade.domain.entity.AuditRecord;
import com.fruittrade.domain.entity.OrderItem;
import com.fruittrade.domain.entity.Orders;
import com.fruittrade.domain.vo.AfterSaleVO;
import com.fruittrade.mapper.AfterSaleMapper;
import com.fruittrade.mapper.AuditRecordMapper;
import com.fruittrade.mapper.OrderItemMapper;
import com.fruittrade.mapper.OrdersMapper;
import com.fruittrade.service.AfterSaleService;
import com.fruittrade.service.NumberGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 售后服务实现。
 */
@Service
public class AfterSaleServiceImpl implements AfterSaleService {

    private final AfterSaleMapper afterSaleMapper;
    private final OrdersMapper ordersMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final OrderItemMapper orderItemMapper;

    public AfterSaleServiceImpl(AfterSaleMapper afterSaleMapper, OrdersMapper ordersMapper,
                                AuditRecordMapper auditRecordMapper, OrderItemMapper orderItemMapper) {
        this.afterSaleMapper = afterSaleMapper;
        this.ordersMapper = ordersMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional
    public Long apply(Long userId, AfterSaleDTO dto) {
        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "订单不存在或非本人订单");
        }
        // 售后限完成后 7 天内申请
        if (order.getFinishTime() == null
                || ChronoUnit.DAYS.between(order.getFinishTime(), LocalDateTime.now()) > 7) {
            throw new BusinessException(ErrorCode.AFTER_SALE_TIMEOUT);
        }
        // 前端按契约传 productId，后端存 order_item_id：根据订单+商品定位明细
        Long orderItemId = dto.getOrderItemId();
        if (orderItemId == null && dto.getProductId() != null) {
            OrderItem oi = orderItemMapper.selectOne(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, order.getId())
                            .eq(OrderItem::getProductId, dto.getProductId())
                            .eq(OrderItem::getDeleted, 0)
                            .last("limit 1"));
            orderItemId = oi == null ? null : oi.getId();
        }
        AfterSale as = new AfterSale();
        as.setAfterSaleNo(NumberGen.afterSaleNo());
        as.setOrderId(order.getId());
        as.setOrderItemId(orderItemId);
        as.setUserId(userId);
        as.setMerchantId(order.getMerchantId());
        as.setType(dto.getType());
        as.setReason(dto.getReason());
        as.setDescription(dto.getDescription());
        as.setImages(toJson(dto.getImages()));
        as.setAmount(dto.getAmount() == null ? order.getPayAmount() : dto.getAmount());
        as.setStatus(0);
        as.setDeleted(0);
        afterSaleMapper.insert(as);
        return as.getId();
    }

    @Override
    public PageResult<AfterSaleVO> userList(Long userId, Long pageNum, Long pageSize) {
        IPage<AfterSale> page = afterSaleMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AfterSale>()
                        .eq(AfterSale::getUserId, userId)
                        .eq(AfterSale::getDeleted, 0)
                        .orderByDesc(AfterSale::getId));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public PageResult<AfterSaleVO> merchantList(Long merchantId, Long pageNum, Long pageSize) {
        IPage<AfterSale> page = afterSaleMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AfterSale>()
                        .eq(AfterSale::getMerchantId, merchantId)
                        .eq(AfterSale::getDeleted, 0)
                        .orderByDesc(AfterSale::getId));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional
    public void handle(Long merchantId, Long id, AuditDTO dto, Long handlerId) {
        AfterSale as = afterSaleMapper.selectById(id);
        if (as == null || !as.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        int before = as.getStatus();
        int after = dto.getStatus() != null ? dto.getStatus()
                : ("REJECT".equalsIgnoreCase(dto.getAction()) ? 4 : 1);
        as.setStatus(after);
        as.setHandleRemark(dto.effectiveRemark());
        as.setHandlerId(handlerId);
        as.setHandleTime(LocalDateTime.now());
        afterSaleMapper.updateById(as);

        AuditRecord record = new AuditRecord();
        record.setBizType("AFTER_SALE");
        record.setBizId(id);
        record.setAuditorId(handlerId);
        record.setAction(after == 4 ? "REJECT" : "APPROVE");
        record.setBeforeStatus(before);
        record.setAfterStatus(after);
        record.setRemark(dto.effectiveRemark());
        record.setAuditTime(LocalDateTime.now());
        record.setDeleted(0);
        auditRecordMapper.insert(record);
    }

    private AfterSaleVO toVO(AfterSale a) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(a.getId());
        vo.setAfterSaleNo(a.getAfterSaleNo());
        vo.setOrderId(a.getOrderId());
        vo.setOrderItemId(a.getOrderItemId());
        vo.setUserId(a.getUserId());
        vo.setMerchantId(a.getMerchantId());
        vo.setType(a.getType());
        vo.setTypeText(a.getType() == null ? "" :
                switch (a.getType()) {
                    case 1 -> "仅退款";
                    case 2 -> "退货退款";
                    case 3 -> "换货";
                    default -> "未知";
                });
        vo.setReason(a.getReason());
        vo.setDescription(a.getDescription());
        vo.setAmount(a.getAmount());
        vo.setStatus(a.getStatus());
        vo.setStatusText(statusText(a.getStatus()));
        vo.setHandleRemark(a.getHandleRemark());
        vo.setHandleTime(a.getHandleTime());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }

    /** P1 口径：待审核/已同意(审核通过或处理中)/已完成/已驳回 */
    public static String statusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待审核";
            case 1, 2 -> "已同意";
            case 3 -> "已完成";
            case 4 -> "已驳回";
            default -> "未知";
        };
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }
}
