package com.DB.DBmarket;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.mapper.AfterSaleTicketMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicket;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicketView;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.service.impl.AfterSaleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AfterSaleServiceImplTests {

    @Mock
    private AfterSaleTicketMapper afterSaleTicketMapper;

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AfterSaleServiceImpl afterSaleService;

    @Test
    void customerCanCreateAfterSaleTicket() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo order = buildOrder("order-1", "cus001", "mer001", 2);
        AfterSaleTicket stored = new AfterSaleTicket("ticket-1", "order-1", "cus001", "mer001", "投诉反馈", "餐品有问题", "待处理", null, null, LocalDateTime.now(), LocalDateTime.now());

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(order));
        when(afterSaleTicketMapper.listByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(userMapper.getNameById("cus001")).thenReturn("customer");
        when(userMapper.getNameById("mer001")).thenReturn("merchant");
        when(orderInfoMapper.getOrderById("order-1")).thenReturn(order);
        when(addressMapper.getAddressByAddressId("addr-cus")).thenReturn("Dormitory");
        when(addressMapper.getAddressByAddressId("addr-mer")).thenReturn("Kitchen");

        AfterSaleTicketView result = afterSaleService.createTicket(customer, "order-1", "投诉反馈", "餐品有问题");

        assertNotNull(result);
        assertEquals("投诉反馈", result.getType());
        assertEquals("待处理", result.getStatus());
        verify(afterSaleTicketMapper).insert(any(AfterSaleTicket.class));
        verify(orderInfoMapper).updateOrderState(eq("order-1"), eq(2), any(), eq(null), eq(null), eq("1"), eq("餐品有问题"), eq(null));
    }

    @Test
    void merchantCanHandleOwnTicket() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        AfterSaleTicket ticket = new AfterSaleTicket("ticket-1", "order-1", "cus001", "mer001", "退款问题", "想退款", "待处理", null, null, LocalDateTime.now(), LocalDateTime.now());
        AfterSaleTicket updated = new AfterSaleTicket("ticket-1", "order-1", "cus001", "mer001", "退款问题", "想退款", "处理中", "mer001", "已联系顾客处理中", LocalDateTime.now(), LocalDateTime.now());
        OrderInfo order = buildOrder("order-1", "cus001", "mer001", -2);

        when(afterSaleTicketMapper.getById("ticket-1")).thenReturn(ticket, updated);
        when(orderInfoMapper.getOrderById("order-1")).thenReturn(order);
        when(userMapper.getNameById("cus001")).thenReturn("customer");
        when(userMapper.getNameById("mer001")).thenReturn("merchant");
        when(addressMapper.getAddressByAddressId("addr-cus")).thenReturn("Dormitory");
        when(addressMapper.getAddressByAddressId("addr-mer")).thenReturn("Kitchen");
        when(afterSaleTicketMapper.updateHandleInfo(any())).thenReturn(1);

        AfterSaleTicketView result = afterSaleService.updateTicket(merchant, "ticket-1", "处理中", "已联系顾客处理中");

        assertNotNull(result);
        assertEquals("处理中", result.getStatus());
        assertEquals("merchant", result.getHandlerName());
        verify(afterSaleTicketMapper).updateHandleInfo(any(AfterSaleTicket.class));
    }

    private OrderInfo buildOrder(String id, String cus, String mer, Integer state) {
        OrderInfo order = new OrderInfo();
        order.setId(id);
        order.setCus(cus);
        order.setMer(mer);
        order.setState(state);
        order.setAccount(36);
        order.setRecAddr("addr-cus");
        order.setDeliAddr("addr-mer");
        order.setTime(LocalDateTime.now());
        return order;
    }
}
