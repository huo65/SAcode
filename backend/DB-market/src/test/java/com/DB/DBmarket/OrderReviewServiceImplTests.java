package com.DB.DBmarket;

import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.OrderReviewMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.service.impl.OrderReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReviewServiceImplTests {

    @Mock
    private OrderReviewMapper orderReviewMapper;

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private OrderReviewServiceImpl orderReviewService;

    @Test
    void customerCanReviewCompletedOwnOrder() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo completedOrder = buildOrder("order-1", "cus001", "mer001", 2);
        OrderReview saved = new OrderReview("order-1", "cus001", "mer001", 5, "great", null, null, null, "customer");

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(completedOrder));
        when(orderReviewMapper.getByOrderId("order-1")).thenReturn(null, saved);
        when(userMapper.getNameById("cus001")).thenReturn("customer");

        OrderReview result = orderReviewService.submitReview(customer, new OrderReview("order-1", null, null, 5, "great", null, null, null, null));

        assertNotNull(result);
        assertEquals("customer", result.getCustomerName());
        verify(orderReviewMapper).insert(any(OrderReview.class));
    }

    @Test
    void reviewRejectsNonCompletedOrder() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo deliveringOrder = buildOrder("order-1", "cus001", "mer001", 1);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(deliveringOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderReviewService.submitReview(customer, new OrderReview("order-1", null, null, 5, "great", null, null, null, null)));

        assertEquals("Only completed orders can be reviewed.", ex.getMessage());
        verify(orderReviewMapper, never()).insert(any(OrderReview.class));
    }

    @Test
    void reviewRejectsDuplicateOrderReview() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo completedOrder = buildOrder("order-1", "cus001", "mer001", 2);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(completedOrder));
        when(orderReviewMapper.getByOrderId("order-1")).thenReturn(new OrderReview());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderReviewService.submitReview(customer, new OrderReview("order-1", null, null, 5, "great", null, null, null, null)));

        assertEquals("This order has already been reviewed.", ex.getMessage());
        verify(orderReviewMapper, never()).insert(any(OrderReview.class));
    }

    @Test
    void merchantCanReplyOwnReview() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderReview existing = new OrderReview("order-1", "cus001", "mer001", 5, "great", null, null, null, null);
        OrderReview replied = new OrderReview("order-1", "cus001", "mer001", 5, "great", null, "thanks", null, "customer");

        when(orderReviewMapper.getByOrderId("order-1")).thenReturn(existing, replied);
        when(userMapper.getNameById("cus001")).thenReturn("customer");

        OrderReview result = orderReviewService.replyReview(merchant, "order-1", " thanks ");

        assertNotNull(result);
        assertEquals("thanks", result.getReplyContent());
        verify(orderReviewMapper).replyReview(org.mockito.ArgumentMatchers.eq("order-1"), org.mockito.ArgumentMatchers.eq("thanks"), any(java.time.LocalDateTime.class));
    }

    @Test
    void replyRejectsSecondReply() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderReview existing = new OrderReview("order-1", "cus001", "mer001", 5, "great", null, "already replied", null, null);

        when(orderReviewMapper.getByOrderId("order-1")).thenReturn(existing);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderReviewService.replyReview(merchant, "order-1", "thanks"));

        assertEquals("This review has already been replied.", ex.getMessage());
        verify(orderReviewMapper, never()).replyReview(org.mockito.ArgumentMatchers.eq("order-1"), org.mockito.ArgumentMatchers.eq("thanks"), any(java.time.LocalDateTime.class));
    }

    private OrderInfo buildOrder(String id, String cus, String mer, Integer state) {
        OrderInfo order = new OrderInfo();
        order.setId(id);
        order.setCus(cus);
        order.setMer(mer);
        order.setState(state);
        return order;
    }
}
