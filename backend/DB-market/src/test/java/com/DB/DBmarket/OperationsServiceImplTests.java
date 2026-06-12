package com.DB.DBmarket;

import com.DB.DBmarket.mapper.AfterSaleTicketMapper;
import com.DB.DBmarket.mapper.OperationAuditLogMapper;
import com.DB.DBmarket.mapper.OperationPermissionMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicket;
import com.DB.DBmarket.pojo.ops.OperationPermission;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.service.impl.OperationsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsServiceImplTests {

    @Mock
    private OperationPermissionMapper operationPermissionMapper;
    @Mock
    private OperationAuditLogMapper operationAuditLogMapper;
    @Mock
    private OrderInfoMapper orderInfoMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private AfterSaleTicketMapper afterSaleTicketMapper;

    @InjectMocks
    private OperationsServiceImpl operationsService;

    @Test
    void getCurrentPermissionSnapshotReturnsEnabledMenusAndActions() {
        CurrentUser admin = new CurrentUser("1", "admin", "admin");
        when(operationPermissionMapper.listByRole("admin")).thenReturn(Arrays.asList(
                new OperationPermission("admin", "admin.menu.goods", "商品治理", "menu", "admin", 1, LocalDateTime.now()),
                new OperationPermission("admin", "admin.action.audit.view", "查看审计日志", "action", "admin", 1, LocalDateTime.now()),
                new OperationPermission("admin", "admin.menu.ops", "治理与经营", "menu", "admin", 0, LocalDateTime.now())
        ));

        Map<String, Object> snapshot = operationsService.getCurrentPermissionSnapshot(admin);

        @SuppressWarnings("unchecked")
        Map<String, Boolean> menuMap = (Map<String, Boolean>) snapshot.get("menuMap");
        @SuppressWarnings("unchecked")
        List<String> actionKeys = (List<String>) snapshot.get("actionKeys");

        assertEquals(Boolean.TRUE, menuMap.get("admin.menu.goods"));
        assertEquals(Boolean.FALSE, menuMap.get("admin.menu.ops"));
        assertTrue(actionKeys.contains("admin.action.audit.view"));
    }

    @Test
    void merchantDashboardAggregatesRevenueAndTopProducts() {
        CurrentUser merchant = new CurrentUser("3", "merchant", "mer");
        when(operationPermissionMapper.getByRoleAndKey("mer", "merchant.action.dashboard.view"))
                .thenReturn(new OperationPermission("mer", "merchant.action.dashboard.view", "查看经营看板",
                        "action", "merchant", 1, LocalDateTime.now()));

        when(orderInfoMapper.getMerOrder("3", null, 1)).thenReturn(new java.util.ArrayList<>(Arrays.asList(
                buildOrder("A100", "3", "p1", 2, 48, 2),
                buildOrder("A100", "3", "p2", 1, 16, 2),
                buildOrder("A101", "3", "p1", 1, 24, 0),
                buildOrder("A102", "3", "p3", 1, 32, -3)
        )));
        when(productMapper.getAllProducts()).thenReturn(Arrays.asList(
                buildProduct("p1", "招牌套餐", "3", 1),
                buildProduct("p2", "清爽饮品", "3", 1),
                buildProduct("p3", "售后样例餐", "3", 1)
        ));
        when(afterSaleTicketMapper.listByMerchant("3", null, null)).thenReturn(Collections.singletonList(
                new AfterSaleTicket("T100", "A102", "2", "3", "退款问题", "少送餐品", "待处理",
                        null, null, LocalDateTime.now(), LocalDateTime.now())
        ));
        when(operationAuditLogMapper.list("3", "mer", null, null)).thenReturn(Collections.emptyList());

        Map<String, Object> dashboard = operationsService.getMerchantDashboard(merchant);

        @SuppressWarnings("unchecked")
        Map<String, Object> overview = (Map<String, Object>) dashboard.get("overview");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) dashboard.get("topProducts");

        assertEquals(88.0, overview.get("gmv"));
        assertEquals(2L, overview.get("paidOrders"));
        assertEquals(1L, overview.get("refundOrders"));
        assertEquals("招牌套餐", topProducts.get(0).get("name"));
    }

    private OrderInfo buildOrder(String orderId, String merchantId, String productId, int qty, int account, int state) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setMer(merchantId);
        orderInfo.setCus("2");
        orderInfo.setProd(productId);
        orderInfo.setProdNum(qty);
        orderInfo.setAccount(account);
        orderInfo.setState(state);
        orderInfo.setTime(LocalDateTime.now());
        return orderInfo;
    }

    private Product buildProduct(String id, String name, String merchantId, int state) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setMer(merchantId);
        product.setState(state);
        return product;
    }
}
