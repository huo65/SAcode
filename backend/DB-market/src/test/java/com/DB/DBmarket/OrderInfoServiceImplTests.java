package com.DB.DBmarket;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.service.impl.OrderInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderInfoServiceImplTests {

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private OrderInfoServiceImpl orderInfoService;

    @Test
    void payOrdersUpdatesStateAndDeductsBalanceForCustomer() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo order = buildOrder("order-1", "cus001", "mer001", "prod001", -1, 2, 60);
        Product product = buildProduct("prod001", 10);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(order));
        when(productMapper.getOneProductByIdForUpdate("prod001")).thenReturn(product);
        when(productMapper.decrementStock("prod001", 2)).thenReturn(1);
        when(userMapper.getBalance("cus001")).thenReturn(200.0, 200.0);

        orderInfoService.payOrders(customer, Collections.singletonList("order-1"));

        verify(productMapper).decrementStock("prod001", 2);
        verify(userMapper).refundOrPay("cus001", 140.0);
        verify(orderInfoMapper).updateOrderState(eq("order-1"), eq(0), anyString(), isNull(), anyString(), isNull(), isNull(), isNull());
    }

    @Test
    void payOrdersRejectsOrdersOutsideCurrentCustomer() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo order = buildOrder("order-1", "cus999", "mer001", "prod001", -1, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.payOrders(customer, Collections.singletonList("order-1")));

        assertEquals("No permission to pay this order.", ex.getMessage());
        verify(productMapper, never()).decrementStock(anyString(), eq(1));
        verify(userMapper, never()).refundOrPay(anyString(), eq(0.0));
    }

    @Test
    void payOrdersRejectsInsufficientBalance() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo order = buildOrder("order-1", "cus001", "mer001", "prod001", -1, 1, 120);
        Product product = buildProduct("prod001", 10);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(order));
        when(productMapper.getOneProductByIdForUpdate("prod001")).thenReturn(product);
        when(userMapper.getBalance("cus001")).thenReturn(100.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.payOrders(customer, Collections.singletonList("order-1")));

        assertEquals("The balance is insufficient.", ex.getMessage());
        verify(productMapper, never()).decrementStock(anyString(), eq(1));
        verify(userMapper, never()).refundOrPay(anyString(), eq(0.0));
        verify(orderInfoMapper, never()).updateOrderState(eq("order-1"), eq(0), anyString(), isNull(), anyString(), isNull(), isNull(), isNull());
    }

    @Test
    void merchantCanAcceptPaidOrderForPreparation() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo paidOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 0, 1, 30);
        OrderInfo updatedOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 4, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Collections.singletonList(paidOrder))
                .thenReturn(Collections.singletonList(updatedOrder));
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(4), anyString(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(merchant, "order-1", 4, null, null, null);

        assertNotNull(result);
        assertEquals(Integer.valueOf(4), result.getState());
    }

    @Test
    void merchantCanSendPreparingOrderToDriverPool() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo preparingOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 4, 1, 30);
        OrderInfo updatedOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 3, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Collections.singletonList(preparingOrder))
                .thenReturn(Collections.singletonList(updatedOrder));
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(3), anyString(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(merchant, "order-1", 3, null, null, null);

        assertNotNull(result);
        assertEquals(Integer.valueOf(3), result.getState());
    }

    @Test
    void driverCanTakeWaitingOrderAndBindDriverId() {
        CurrentUser driver = new CurrentUser("driver001", "driver", "driver");
        OrderInfo waitingOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 3, 1, 30);
        OrderInfo updatedOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 1, 1, 30);
        updatedOrder.setDriverId("driver001");

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Collections.singletonList(waitingOrder))
                .thenReturn(Collections.singletonList(updatedOrder));
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(1), anyString(), eq("driver001"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(driver, "order-1", 1, null, null, null);

        assertNotNull(result);
        assertEquals("driver001", result.getDriverId());
        assertEquals(Integer.valueOf(1), result.getState());
    }

    @Test
    void customerCanConfirmDeliveredOrder() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo deliveringOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 1, 1, 30);
        OrderInfo updatedOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 2, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Collections.singletonList(deliveringOrder))
                .thenReturn(Collections.singletonList(updatedOrder));
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(2), anyString(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(customer, "order-1", 2, null, null, null);

        assertNotNull(result);
        assertEquals(Integer.valueOf(2), result.getState());
    }

    @Test
    void refundConfirmationRestoresBalanceAndStock() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo refundingA = buildOrder("order-1", "cus001", "mer001", "prod001", -2, 2, 60);
        OrderInfo refundingB = buildOrder("order-1", "cus001", "mer001", "prod002", -2, 1, 40);
        OrderInfo refunded = buildOrder("order-1", "cus001", "mer001", "prod001", -3, 2, 60);

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Arrays.asList(refundingA, refundingB))
                .thenReturn(Collections.singletonList(refunded));
        when(userMapper.getBalance("cus001")).thenReturn(50.0);
        when(orderInfoMapper.getOrderAccount("order-1")).thenReturn(100);
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(-3), anyString(), isNull(), isNull(), isNull(), isNull(), eq("bad food")))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(merchant, "order-1", -3, null, null, "bad food");

        assertNotNull(result);
        assertEquals(Integer.valueOf(-3), result.getState());
        verify(userMapper).refundOrPay("cus001", 150.0);
        verify(productMapper).incrementStock("prod001", 2);
        verify(productMapper).incrementStock("prod002", 1);
    }

    @Test
    void merchantCanRejectPaidOrderAndRefundImmediately() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo paidA = buildOrder("order-1", "cus001", "mer001", "prod001", 0, 2, 60);
        OrderInfo paidB = buildOrder("order-1", "cus001", "mer001", "prod002", 0, 1, 40);
        OrderInfo refunded = buildOrder("order-1", "cus001", "mer001", "prod001", -3, 2, 60);

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Arrays.asList(paidA, paidB))
                .thenReturn(Collections.singletonList(refunded));
        when(userMapper.getBalance("cus001")).thenReturn(50.0);
        when(orderInfoMapper.getOrderAccount("order-1")).thenReturn(100);
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(-3), anyString(), isNull(), isNull(), isNull(), isNull(), eq("merchant rejected")))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(merchant, "order-1", -3, null, null, "merchant rejected");

        assertNotNull(result);
        assertEquals(Integer.valueOf(-3), result.getState());
        verify(userMapper).refundOrPay("cus001", 150.0);
        verify(productMapper).incrementStock("prod001", 2);
        verify(productMapper).incrementStock("prod002", 1);
    }

    @Test
    void refundConfirmationRejectsWrongCurrentState() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo unpaidOrder = buildOrder("order-1", "cus001", "mer001", "prod001", -1, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(unpaidOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.transitionOrder(merchant, "order-1", -3, null, null, "bad food"));

        assertEquals("Only paid or refunding orders can be refunded.", ex.getMessage());
        verify(userMapper, never()).refundOrPay(anyString(), eq(0.0));
        verify(productMapper, never()).incrementStock(anyString(), eq(1));
    }

    @Test
    void paidOrderRejectRequiresRefundReason() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo paidOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 0, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(paidOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.transitionOrder(merchant, "order-1", -3, null, null, " "));

        assertEquals("Rejecting a paid order requires a refund reason.", ex.getMessage());
        verify(userMapper, never()).refundOrPay(anyString(), eq(0.0));
    }

    @Test
    void transitionOrderRejectsCreationOrPaymentStates() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo unpaidOrder = buildOrder("order-1", "cus001", "mer001", "prod001", -1, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(unpaidOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.transitionOrder(customer, "order-1", 0, null, null, null));

        assertEquals("Use order creation or payment endpoint for this state.", ex.getMessage());
        verify(orderInfoMapper, never()).updateOrderState(eq("order-1"), eq(0), anyString(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void transitionOrderRejectsUnauthorizedMerchantDeliveryAction() {
        CurrentUser wrongMerchant = new CurrentUser("mer999", "merchant", "mer");
        OrderInfo paidOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 0, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(paidOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.transitionOrder(wrongMerchant, "order-1", 4, null, null, null));

        assertEquals("Only the merchant can accept a paid order for preparation.", ex.getMessage());
        verify(orderInfoMapper, never()).updateOrderState(eq("order-1"), eq(4), anyString(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void merchantCannotSendPaidOrderDirectlyToDriverPool() {
        CurrentUser merchant = new CurrentUser("mer001", "merchant", "mer");
        OrderInfo paidOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 0, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(paidOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.transitionOrder(merchant, "order-1", 3, null, null, null));

        assertEquals("Only the merchant can send a preparing order to drivers.", ex.getMessage());
        verify(orderInfoMapper, never()).updateOrderState(eq("order-1"), eq(3), anyString(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void driverCannotTakeOrderOutsideWaitingState() {
        CurrentUser driver = new CurrentUser("driver001", "driver", "driver");
        OrderInfo deliveringOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 1, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(deliveringOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderInfoService.transitionOrder(driver, "order-1", 1, null, null, null));

        assertEquals("Only a driver can take a waiting delivery order.", ex.getMessage());
    }

    @Test
    void refundRequestPassesRefundReasonToOrderUpdate() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo deliveringOrder = buildOrder("order-1", "cus001", "mer001", "prod001", 1, 1, 30);
        OrderInfo refundingOrder = buildOrder("order-1", "cus001", "mer001", "prod001", -2, 1, 30);

        when(orderInfoMapper.getOrdersById("order-1"))
                .thenReturn(Collections.singletonList(deliveringOrder))
                .thenReturn(Collections.singletonList(refundingOrder));
        when(orderInfoMapper.updateOrderState(eq("order-1"), eq(-2), anyString(), isNull(), isNull(), isNull(), isNull(), eq("too late")))
                .thenReturn(1);

        OrderInfo result = orderInfoService.transitionOrder(customer, "order-1", -2, null, null, "too late");

        assertNotNull(result);
        assertEquals(Integer.valueOf(-2), result.getState());
    }

    @Test
    void payOrdersDeduplicatesRepeatedOrderIdsBeforeUpdatingState() {
        CurrentUser customer = new CurrentUser("cus001", "customer", "cus");
        OrderInfo order = buildOrder("order-1", "cus001", "mer001", "prod001", -1, 1, 30);
        Product product = buildProduct("prod001", 10);

        when(orderInfoMapper.getOrdersById("order-1")).thenReturn(Collections.singletonList(order));
        when(productMapper.getOneProductByIdForUpdate("prod001")).thenReturn(product);
        when(productMapper.decrementStock("prod001", 1)).thenReturn(1);
        when(userMapper.getBalance("cus001")).thenReturn(100.0, 100.0);

        orderInfoService.payOrders(customer, Arrays.asList("order-1", "order-1"));

        verify(productMapper).decrementStock("prod001", 1);
        ArgumentCaptor<String> orderIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderInfoMapper).updateOrderState(orderIdCaptor.capture(), eq(0), anyString(), isNull(), anyString(), isNull(), isNull(), isNull());
        assertEquals("order-1", orderIdCaptor.getValue());
    }

    private static OrderInfo buildOrder(String id, String cus, String mer, String prod, int state, int prodNum, int account) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(id);
        orderInfo.setCus(cus);
        orderInfo.setMer(mer);
        orderInfo.setProd(prod);
        orderInfo.setState(state);
        orderInfo.setProdNum(prodNum);
        orderInfo.setAccount(account);
        return orderInfo;
    }

    private static Product buildProduct(String id, int number) {
        Product product = new Product();
        product.setId(id);
        product.setNumber(number);
        product.setState(1);
        return product;
    }
}
