package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.mapper.AfterSaleTicketMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicket;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicketView;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.service.AfterSaleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AfterSaleServiceImpl implements AfterSaleService {
    private static final String STATUS_PENDING = "待处理";
    private static final String STATUS_PROCESSING = "处理中";
    private static final String STATUS_RESOLVED = "已解决";
    private static final String STATUS_CLOSED = "已关闭";

    @Resource
    private AfterSaleTicketMapper afterSaleTicketMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AddressMapper addressMapper;

    @Override
    public AfterSaleTicketView createTicket(CurrentUser currentUser, String orderId, String type, String content) {
        ensureTable();
        if (!currentUser.isCustomer()) {
            throw new IllegalArgumentException("Only customer can create after-sale tickets.");
        }
        if (isBlank(orderId) || isBlank(type) || isBlank(content)) {
            throw new IllegalArgumentException("Order, type and content are required.");
        }
        List<OrderInfo> rows = orderInfoMapper.getOrdersById(orderId);
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Order does not exist.");
        }
        OrderInfo first = rows.get(0);
        if (!currentUser.getId().equals(first.getCus())) {
            throw new IllegalArgumentException("You can only create tickets for your own orders.");
        }
        if (first.getState() == null || (first.getState() != 2 && first.getState() != -2 && first.getState() != -3 && first.getState() != 1)) {
            throw new IllegalArgumentException("Current order stage does not support after-sale submission.");
        }
        List<AfterSaleTicket> existing = afterSaleTicketMapper.listByOrderId(orderId);
        for (AfterSaleTicket ticket : existing) {
            if (!STATUS_CLOSED.equals(ticket.getStatus()) && !STATUS_RESOLVED.equals(ticket.getStatus())) {
                throw new IllegalArgumentException("This order already has an active after-sale ticket.");
            }
        }
        LocalDateTime now = LocalDateTime.now();
        AfterSaleTicket ticket = new AfterSaleTicket(
                RandomIdGenerator.getRandomId(),
                orderId,
                currentUser.getId(),
                first.getMer(),
                type.trim(),
                content.trim(),
                STATUS_PENDING,
                null,
                null,
                now,
                now
        );
        afterSaleTicketMapper.insert(ticket);
        markOrderComplaint(first, type, content);
        return enrich(ticket);
    }

    @Override
    public List<AfterSaleTicketView> listTickets(CurrentUser currentUser, String scope, String status, String type) {
        ensureTable();
        String normalizedScope = normalizeScope(currentUser, scope);
        List<AfterSaleTicket> tickets;
        switch (normalizedScope) {
            case "merchant":
                tickets = afterSaleTicketMapper.listByMerchant(currentUser.getId(), status, type);
                break;
            case "admin":
                tickets = afterSaleTicketMapper.listAll(status, type);
                break;
            default:
                tickets = afterSaleTicketMapper.listByCustomer(currentUser.getId(), status, type);
                break;
        }
        List<AfterSaleTicketView> result = new ArrayList<>();
        for (AfterSaleTicket ticket : tickets) {
            result.add(enrich(ticket));
        }
        return result;
    }

    @Override
    public AfterSaleTicketView updateTicket(CurrentUser currentUser, String id, String status, String handlerNote) {
        ensureTable();
        if (isBlank(id) || isBlank(status)) {
            throw new IllegalArgumentException("Ticket id and status are required.");
        }
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid ticket status.");
        }
        AfterSaleTicket ticket = afterSaleTicketMapper.getById(id);
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket not found.");
        }
        if (currentUser.isMerchant()) {
            if (!currentUser.getId().equals(ticket.getMerchantId())) {
                throw new IllegalArgumentException("You can only handle tickets for your own store.");
            }
            if (STATUS_CLOSED.equals(status)) {
                throw new IllegalArgumentException("Merchant cannot directly close a ticket.");
            }
        } else if (!currentUser.isAdmin()) {
            throw new IllegalArgumentException("Only merchant or admin can handle tickets.");
        }
        ticket.setStatus(status);
        ticket.setHandlerId(currentUser.getId());
        ticket.setHandlerNote(isBlank(handlerNote) ? ticket.getHandlerNote() : handlerNote.trim());
        ticket.setUpdatedTime(LocalDateTime.now());
        int affected = afterSaleTicketMapper.updateHandleInfo(ticket);
        if (affected <= 0) {
            throw new IllegalArgumentException("Failed to update ticket.");
        }
        return enrich(afterSaleTicketMapper.getById(id));
    }

    @Override
    public Map<String, Object> getStats(CurrentUser currentUser, String scope) {
        String normalizedScope = normalizeScope(currentUser, scope);
        List<AfterSaleTicketView> tickets = listTickets(currentUser, normalizedScope, null, null);
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", tickets.size());
        stats.put("pending", tickets.stream().filter(item -> STATUS_PENDING.equals(item.getStatus())).count());
        stats.put("processing", tickets.stream().filter(item -> STATUS_PROCESSING.equals(item.getStatus())).count());
        stats.put("resolved", tickets.stream().filter(item -> STATUS_RESOLVED.equals(item.getStatus())).count());
        stats.put("refund", tickets.stream().filter(item -> "退款问题".equals(item.getType())).count());
        stats.put("complaint", tickets.stream().filter(item -> "投诉反馈".equals(item.getType())).count());
        return stats;
    }

    private void ensureTable() {
        try {
            afterSaleTicketMapper.createTableIfMissing();
        } catch (Exception ignore) {
            // keep compatible with legacy database state
        }
    }

    private void markOrderComplaint(OrderInfo order, String type, String content) {
        String complain = "投诉反馈".equals(type) || "配送问题".equals(type) ? "1" : order.getComplain();
        String complainReason = "投诉反馈".equals(type) || "配送问题".equals(type) ? content : order.getComplainReason();
        String refundReason = "退款问题".equals(type) ? content : order.getRefundReason();
        orderInfoMapper.updateOrderState(
                order.getId(),
                order.getState(),
                String.valueOf(LocalDateTime.now()),
                order.getDriverId(),
                order.getPayTime() == null ? null : String.valueOf(order.getPayTime()),
                complain,
                complainReason,
                refundReason
        );
    }

    private AfterSaleTicketView enrich(AfterSaleTicket ticket) {
        AfterSaleTicketView view = new AfterSaleTicketView();
        view.setId(ticket.getId());
        view.setOrderId(ticket.getOrderId());
        view.setCustomerId(ticket.getCustomerId());
        view.setMerchantId(ticket.getMerchantId());
        view.setType(ticket.getType());
        view.setContent(ticket.getContent());
        view.setStatus(ticket.getStatus());
        view.setHandlerId(ticket.getHandlerId());
        view.setHandlerNote(ticket.getHandlerNote());
        view.setCreatedTime(ticket.getCreatedTime());
        view.setUpdatedTime(ticket.getUpdatedTime());
        view.setCustomerName(userMapper.getNameById(ticket.getCustomerId()));
        view.setMerchantName(userMapper.getNameById(ticket.getMerchantId()));
        view.setHandlerName(isBlank(ticket.getHandlerId()) ? null : userMapper.getNameById(ticket.getHandlerId()));
        OrderInfo order = orderInfoMapper.getOrderById(ticket.getOrderId());
        if (order != null) {
            view.setOrderState(order.getState());
            view.setOrderAmount(order.getAccount());
            view.setOrderTime(order.getTime() == null ? null : String.valueOf(order.getTime()));
            view.setReceiveAddress(addressMapper.getAddressByAddressId(order.getRecAddr()));
            view.setDeliveryAddress(addressMapper.getAddressByAddressId(order.getDeliAddr()));
        }
        return view;
    }

    private String normalizeScope(CurrentUser currentUser, String scope) {
        if (currentUser.isAdmin() && "admin".equals(scope)) {
            return "admin";
        }
        if (currentUser.isMerchant() && "merchant".equals(scope)) {
            return "merchant";
        }
        return "customer";
    }

    private boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status)
                || STATUS_PROCESSING.equals(status)
                || STATUS_RESOLVED.equals(status)
                || STATUS_CLOSED.equals(status);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
