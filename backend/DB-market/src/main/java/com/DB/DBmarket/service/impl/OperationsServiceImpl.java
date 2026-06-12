package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.AfterSaleTicketMapper;
import com.DB.DBmarket.mapper.OperationAuditLogMapper;
import com.DB.DBmarket.mapper.OperationPermissionMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicket;
import com.DB.DBmarket.pojo.ops.OperationAuditLog;
import com.DB.DBmarket.pojo.ops.OperationPermission;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.service.OperationsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service("OperationsService")
public class OperationsServiceImpl implements OperationsService {
    private static final String TYPE_MENU = "menu";
    private static final String TYPE_ACTION = "action";
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final Set<Integer> ACTIVE_ORDER_STATES = new LinkedHashSet<>();
    private static final Set<Integer> REFUND_ORDER_STATES = new LinkedHashSet<>();

    static {
        Collections.addAll(ACTIVE_ORDER_STATES, 0, 1, 2, 3, 4);
        Collections.addAll(REFUND_ORDER_STATES, -2, -3);
    }

    @Resource
    private OperationPermissionMapper operationPermissionMapper;
    @Resource
    private OperationAuditLogMapper operationAuditLogMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private AfterSaleTicketMapper afterSaleTicketMapper;

    @Override
    public Map<String, Object> getCurrentPermissionSnapshot(CurrentUser currentUser) {
        ensureInitialized();
        if (currentUser == null) {
            return emptyPermissionSnapshot();
        }
        List<OperationPermission> permissions = operationPermissionMapper.listByRole(currentUser.getType());
        return buildPermissionSnapshot(currentUser.getType(), permissions);
    }

    @Override
    public Map<String, Object> listPermissionMatrix(CurrentUser currentUser) {
        ensureInitialized();
        requireAdminPermission(currentUser, "admin.action.permission.manage");
        Map<String, Object> data = new LinkedHashMap<>();
        List<Map<String, Object>> roleGroups = new ArrayList<>();
        roleGroups.add(buildRoleGroup("admin", "管理员"));
        roleGroups.add(buildRoleGroup("mer", "商家"));
        data.put("roleGroups", roleGroups);
        return data;
    }

    @Override
    public void updatePermission(CurrentUser currentUser, String roleCode, String permissionKey, Integer enabled) {
        ensureInitialized();
        requireAdminPermission(currentUser, "admin.action.permission.manage");
        if (roleCode == null || roleCode.trim().isEmpty() || permissionKey == null || permissionKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission request is invalid.");
        }
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new IllegalArgumentException("Permission switch must be 0 or 1.");
        }
        OperationPermission existing = operationPermissionMapper.getByRoleAndKey(roleCode.trim(), permissionKey.trim());
        if (existing == null) {
            throw new IllegalArgumentException("Permission does not exist.");
        }
        operationPermissionMapper.updateEnabled(roleCode.trim(), permissionKey.trim(), enabled, LocalDateTime.now());
        recordAudit(currentUser, "PERMISSION_UPDATE", "permission", permissionKey.trim(),
                existing.getPermissionName(), "角色 " + roleCode.trim() + " 调整为 " + enabled, RESULT_SUCCESS);
    }

    @Override
    public boolean hasPermission(CurrentUser currentUser, String permissionKey) {
        ensureInitialized();
        if (currentUser == null || permissionKey == null || permissionKey.trim().isEmpty()) {
            return false;
        }
        OperationPermission permission = operationPermissionMapper.getByRoleAndKey(currentUser.getType(), permissionKey.trim());
        return permission != null && Integer.valueOf(1).equals(permission.getEnabled());
    }

    @Override
    public Map<String, Object> getAdminDashboard(CurrentUser currentUser) {
        ensureInitialized();
        requireAdminPermission(currentUser, "admin.action.dashboard.view");
        List<OrderBundle> bundles = aggregateOrders(orderInfoMapper.getAllOrders());
        List<User> users = safeList(userMapper.listUsers(null));
        List<Product> products = safeList(productMapper.getAllProducts());
        afterSaleTicketMapper.createTableIfMissing();
        List<AfterSaleTicket> tickets = safeList(afterSaleTicketMapper.listAll(null, null));
        List<OperationAuditLog> auditLogs = safeList(operationAuditLogMapper.list(null, null, null, null));

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("gmv", roundMoney(sumBundleAmount(filterBundlesByStates(bundles, ACTIVE_ORDER_STATES))));
        overview.put("paidOrders", countBundlesByStates(bundles, ACTIVE_ORDER_STATES));
        overview.put("refundOrders", countBundlesByStates(bundles, REFUND_ORDER_STATES));
        overview.put("refundAmount", roundMoney(sumBundleAmount(filterBundlesByStates(bundles, REFUND_ORDER_STATES))));
        overview.put("pendingAfterSale", countTicketsByStatus(tickets, "待处理"));
        overview.put("processingAfterSale", countTicketsByStatus(tickets, "处理中"));
        overview.put("totalUsers", users.size());
        overview.put("activeMerchants", countUsersByType(users, "mer"));
        overview.put("customers", countUsersByType(users, "cus"));
        overview.put("drivers", countUsersByType(users, "driver"));
        overview.put("approvedProducts", products.stream().filter(item -> item.getState() != null && item.getState() == 1).count());

        Map<String, Object> roleStats = new LinkedHashMap<>();
        roleStats.put("admin", countUsersByType(users, "admin"));
        roleStats.put("merchant", countUsersByType(users, "mer"));
        roleStats.put("customer", countUsersByType(users, "cus"));
        roleStats.put("driver", countUsersByType(users, "driver"));

        Map<String, Object> permissionSummary = new LinkedHashMap<>();
        permissionSummary.put("adminMenus", countPermissions("admin", TYPE_MENU));
        permissionSummary.put("adminActions", countPermissions("admin", TYPE_ACTION));
        permissionSummary.put("merchantMenus", countPermissions("mer", TYPE_MENU));
        permissionSummary.put("merchantActions", countPermissions("mer", TYPE_ACTION));

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("overview", overview);
        dashboard.put("roleStats", roleStats);
        dashboard.put("permissionSummary", permissionSummary);
        dashboard.put("dailyTrend", buildDailyTrend(bundles));
        dashboard.put("orderStateDistribution", buildOrderStateDistribution(bundles));
        dashboard.put("recentAudits", limitLogs(auditLogs, 8));
        dashboard.put("recentOrders", buildRecentOrders(bundles, 6, null));
        return dashboard;
    }

    @Override
    public Map<String, Object> getMerchantDashboard(CurrentUser currentUser) {
        ensureInitialized();
        requireMerchantPermission(currentUser, "merchant.action.dashboard.view");
        List<OrderBundle> bundles = aggregateOrders(orderInfoMapper.getMerOrder(currentUser.getId(), null, 1));
        List<Product> allProducts = safeList(productMapper.getAllProducts());
        afterSaleTicketMapper.createTableIfMissing();
        List<AfterSaleTicket> tickets = safeList(afterSaleTicketMapper.listByMerchant(currentUser.getId(), null, null));
        List<OperationAuditLog> auditLogs = safeList(operationAuditLogMapper.list(currentUser.getId(), currentUser.getType(), null, null));

        Map<String, Product> productMap = new LinkedHashMap<>();
        for (Product product : allProducts) {
            if (product != null && currentUser.getId().equals(product.getMer())) {
                productMap.put(product.getId(), product);
            }
        }

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("gmv", roundMoney(sumBundleAmount(filterBundlesByStates(bundles, ACTIVE_ORDER_STATES))));
        overview.put("paidOrders", countBundlesByStates(bundles, ACTIVE_ORDER_STATES));
        overview.put("completedOrders", countBundlesByState(bundles, 2));
        overview.put("refundOrders", countBundlesByStates(bundles, REFUND_ORDER_STATES));
        overview.put("refundAmount", roundMoney(sumBundleAmount(filterBundlesByStates(bundles, REFUND_ORDER_STATES))));
        overview.put("pendingAfterSale", countTicketsByStatus(tickets, "待处理"));
        overview.put("processingAfterSale", countTicketsByStatus(tickets, "处理中"));
        overview.put("approvedProducts", productMap.values().stream().filter(item -> item.getState() != null && item.getState() == 1).count());
        overview.put("avgOrderAmount", roundMoney(averageBundleAmount(filterBundlesByStates(bundles, ACTIVE_ORDER_STATES))));

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("overview", overview);
        dashboard.put("dailyTrend", buildDailyTrend(bundles));
        dashboard.put("topProducts", buildTopProducts(bundles, productMap));
        dashboard.put("recentOrders", buildRecentOrders(bundles, 6, currentUser.getId()));
        dashboard.put("recentAudits", limitLogs(auditLogs, 8));
        dashboard.put("ticketBreakdown", buildTicketBreakdown(tickets));
        return dashboard;
    }

    @Override
    public Map<String, Object> exportMerchantDashboard(CurrentUser currentUser) {
        ensureInitialized();
        requireMerchantPermission(currentUser, "merchant.action.report.export");
        Map<String, Object> dashboard = getMerchantDashboard(currentUser);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trend = (List<Map<String, Object>>) dashboard.get("dailyTrend");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) dashboard.get("topProducts");
        StringBuilder csv = new StringBuilder();
        csv.append("Section,Name,Value,Extra\n");
        @SuppressWarnings("unchecked")
        Map<String, Object> overview = (Map<String, Object>) dashboard.get("overview");
        for (Map.Entry<String, Object> entry : overview.entrySet()) {
            csv.append("overview,").append(entry.getKey()).append(",").append(entry.getValue()).append(",\n");
        }
        for (Map<String, Object> row : trend) {
            csv.append("trend,").append(row.get("date")).append(",").append(row.get("gmv")).append(",orders=")
                    .append(row.get("orders")).append("\n");
        }
        for (Map<String, Object> row : topProducts) {
            csv.append("top_product,").append(row.get("name")).append(",").append(row.get("revenue"))
                    .append(",qty=").append(row.get("quantity")).append("\n");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileName", "merchant-operation-report.csv");
        result.put("content", csv.toString());
        return result;
    }

    @Override
    public List<OperationAuditLog> listAuditLogs(CurrentUser currentUser, String scope, String actionType, String keyword) {
        ensureInitialized();
        if (currentUser == null) {
            throw new IllegalArgumentException("Please login first.");
        }
        if (currentUser.isAdmin()) {
            requireAdminPermission(currentUser, "admin.action.audit.view");
            return safeList(operationAuditLogMapper.list(null, null, actionType, keyword));
        }
        if (currentUser.isMerchant()) {
            requireMerchantPermission(currentUser, "merchant.action.audit.view");
            return safeList(operationAuditLogMapper.list(currentUser.getId(), currentUser.getType(), actionType, keyword));
        }
        throw new IllegalArgumentException("Only admin or merchant can view operation audits.");
    }

    @Override
    public void recordAudit(CurrentUser currentUser, String actionType, String targetType, String targetId, String targetName, String detail, String result) {
        ensureInitialized();
        LocalDateTime now = LocalDateTime.now();
        OperationAuditLog log = new OperationAuditLog(
                RandomIdGenerator.getRandomId(),
                currentUser == null ? null : currentUser.getId(),
                currentUser == null ? "system" : currentUser.getName(),
                currentUser == null ? "system" : currentUser.getType(),
                actionType,
                targetType,
                targetId,
                targetName,
                detail,
                result == null || result.trim().isEmpty() ? RESULT_SUCCESS : result.trim(),
                now
        );
        operationAuditLogMapper.insert(log);
    }

    private void ensureInitialized() {
        operationPermissionMapper.createTableIfMissing();
        operationAuditLogMapper.createTableIfMissing();
        LocalDateTime now = LocalDateTime.now();
        for (OperationPermission permission : buildDefaultPermissions(now)) {
            operationPermissionMapper.upsert(permission);
        }
        afterSaleTicketMapper.createTableIfMissing();
    }

    private Map<String, Object> buildRoleGroup(String roleCode, String roleName) {
        List<OperationPermission> permissions = operationPermissionMapper.listByRole(roleCode);
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("roleCode", roleCode);
        group.put("roleName", roleName);
        group.put("permissions", permissions);
        return group;
    }

    private Map<String, Object> buildPermissionSnapshot(String roleCode, List<OperationPermission> permissions) {
        Map<String, Boolean> menuMap = new LinkedHashMap<>();
        Map<String, Boolean> actionMap = new LinkedHashMap<>();
        List<String> menuKeys = new ArrayList<>();
        List<String> actionKeys = new ArrayList<>();
        for (OperationPermission permission : safeList(permissions)) {
            boolean enabled = Integer.valueOf(1).equals(permission.getEnabled());
            if (TYPE_MENU.equals(permission.getPermissionType())) {
                menuMap.put(permission.getPermissionKey(), enabled);
                if (enabled) {
                    menuKeys.add(permission.getPermissionKey());
                }
            } else {
                actionMap.put(permission.getPermissionKey(), enabled);
                if (enabled) {
                    actionKeys.add(permission.getPermissionKey());
                }
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("roleCode", roleCode);
        data.put("menuMap", menuMap);
        data.put("actionMap", actionMap);
        data.put("menuKeys", menuKeys);
        data.put("actionKeys", actionKeys);
        data.put("permissions", permissions);
        return data;
    }

    private Map<String, Object> emptyPermissionSnapshot() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("roleCode", "");
        data.put("menuMap", new LinkedHashMap<>());
        data.put("actionMap", new LinkedHashMap<>());
        data.put("menuKeys", new ArrayList<>());
        data.put("actionKeys", new ArrayList<>());
        data.put("permissions", new ArrayList<>());
        return data;
    }

    private void requireAdminPermission(CurrentUser currentUser, String permissionKey) {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new IllegalArgumentException("Only admin can access this capability.");
        }
        if (!hasPermission(currentUser, permissionKey)) {
            throw new IllegalArgumentException("Admin permission denied: " + permissionKey);
        }
    }

    private void requireMerchantPermission(CurrentUser currentUser, String permissionKey) {
        if (currentUser == null || !currentUser.isMerchant()) {
            throw new IllegalArgumentException("Only merchant can access this capability.");
        }
        if (!hasPermission(currentUser, permissionKey)) {
            throw new IllegalArgumentException("Merchant permission denied: " + permissionKey);
        }
    }

    private List<OrderBundle> aggregateOrders(List<OrderInfo> orders) {
        Map<String, OrderBundle> bundles = new LinkedHashMap<>();
        for (OrderInfo row : safeList(orders)) {
            if (row == null || row.getId() == null) {
                continue;
            }
            OrderBundle bundle = bundles.get(row.getId());
            if (bundle == null) {
                bundle = new OrderBundle();
                bundle.orderId = row.getId();
                bundle.customerId = row.getCus();
                bundle.merchantId = row.getMer();
                bundle.state = row.getState();
                bundle.time = row.getTime();
                bundle.payTime = row.getPayTime();
                bundles.put(row.getId(), bundle);
            }
            bundle.totalAmount += row.getAccount() == null ? 0 : row.getAccount();
            bundle.totalQuantity += row.getProdNum() == null ? 0 : row.getProdNum();
            if (row.getProd() != null) {
                bundle.productQuantities.put(row.getProd(),
                        bundle.productQuantities.getOrDefault(row.getProd(), 0) + (row.getProdNum() == null ? 0 : row.getProdNum()));
                bundle.productRevenue.put(row.getProd(),
                        bundle.productRevenue.getOrDefault(row.getProd(), 0.0) + (row.getAccount() == null ? 0.0 : row.getAccount()));
            }
        }
        List<OrderBundle> result = new ArrayList<>(bundles.values());
        result.sort(Comparator.comparing((OrderBundle item) -> item.time == null ? LocalDateTime.MIN : item.time).reversed());
        return result;
    }

    private List<OrderBundle> filterBundlesByStates(List<OrderBundle> bundles, Set<Integer> states) {
        List<OrderBundle> filtered = new ArrayList<>();
        for (OrderBundle bundle : safeList(bundles)) {
            if (bundle != null && bundle.state != null && states.contains(bundle.state)) {
                filtered.add(bundle);
            }
        }
        return filtered;
    }

    private long countBundlesByState(List<OrderBundle> bundles, int state) {
        return safeList(bundles).stream().filter(item -> item != null && item.state != null && item.state == state).count();
    }

    private long countBundlesByStates(List<OrderBundle> bundles, Set<Integer> states) {
        return filterBundlesByStates(bundles, states).size();
    }

    private double sumBundleAmount(List<OrderBundle> bundles) {
        double total = 0;
        for (OrderBundle bundle : safeList(bundles)) {
            total += bundle.totalAmount;
        }
        return total;
    }

    private double averageBundleAmount(List<OrderBundle> bundles) {
        if (bundles == null || bundles.isEmpty()) {
            return 0;
        }
        return sumBundleAmount(bundles) / bundles.size();
    }

    private long countUsersByType(List<User> users, String type) {
        return safeList(users).stream().filter(item -> item != null && type.equals(item.getType())).count();
    }

    private long countPermissions(String roleCode, String permissionType) {
        return safeList(operationPermissionMapper.listByRole(roleCode)).stream()
                .filter(item -> item != null
                        && permissionType.equals(item.getPermissionType())
                        && Integer.valueOf(1).equals(item.getEnabled()))
                .count();
    }

    private long countTicketsByStatus(List<AfterSaleTicket> tickets, String status) {
        return safeList(tickets).stream().filter(item -> item != null && status.equals(item.getStatus())).count();
    }

    private List<Map<String, Object>> buildDailyTrend(List<OrderBundle> bundles) {
        Map<LocalDate, TrendMetrics> metricsMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            metricsMap.put(today.minusDays(i), new TrendMetrics());
        }
        for (OrderBundle bundle : safeList(bundles)) {
            if (bundle == null || bundle.time == null) {
                continue;
            }
            LocalDate date = bundle.time.toLocalDate();
            TrendMetrics metrics = metricsMap.get(date);
            if (metrics == null) {
                continue;
            }
            if (bundle.state != null && ACTIVE_ORDER_STATES.contains(bundle.state)) {
                metrics.orders += 1;
                metrics.gmv += bundle.totalAmount;
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<LocalDate, TrendMetrics> entry : metricsMap.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", entry.getKey().toString());
            row.put("orders", entry.getValue().orders);
            row.put("gmv", roundMoney(entry.getValue().gmv));
            trend.add(row);
        }
        return trend;
    }

    private List<Map<String, Object>> buildOrderStateDistribution(List<OrderBundle> bundles) {
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        for (OrderBundle bundle : safeList(bundles)) {
            if (bundle == null || bundle.state == null) {
                continue;
            }
            countMap.put(bundle.state, countMap.getOrDefault(bundle.state, 0L) + 1);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : countMap.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("state", entry.getKey());
            row.put("label", describeOrderState(entry.getKey()));
            row.put("count", entry.getValue());
            rows.add(row);
        }
        rows.sort((left, right) -> Long.compare((Long) right.get("count"), (Long) left.get("count")));
        return rows;
    }

    private List<Map<String, Object>> buildRecentOrders(List<OrderBundle> bundles, int limit, String merchantId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (OrderBundle bundle : safeList(bundles)) {
            if (bundle == null) {
                continue;
            }
            if (merchantId != null && !merchantId.equals(bundle.merchantId)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderId", bundle.orderId);
            row.put("state", bundle.state);
            row.put("stateLabel", describeOrderState(bundle.state));
            row.put("amount", roundMoney(bundle.totalAmount));
            row.put("time", bundle.time == null ? "" : bundle.time.toString());
            row.put("itemCount", bundle.totalQuantity);
            rows.add(row);
            if (rows.size() >= limit) {
                break;
            }
        }
        return rows;
    }

    private List<Map<String, Object>> buildTopProducts(List<OrderBundle> bundles, Map<String, Product> productMap) {
        Map<String, Integer> quantityMap = new LinkedHashMap<>();
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        for (OrderBundle bundle : safeList(bundles)) {
            if (bundle == null || bundle.state == null || !ACTIVE_ORDER_STATES.contains(bundle.state)) {
                continue;
            }
            for (Map.Entry<String, Integer> entry : bundle.productQuantities.entrySet()) {
                String productId = entry.getKey();
                quantityMap.put(productId, quantityMap.getOrDefault(productId, 0) + entry.getValue());
                revenueMap.put(productId, revenueMap.getOrDefault(productId, 0.0)
                        + bundle.productRevenue.getOrDefault(productId, 0.0));
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
            Product product = productMap.get(entry.getKey());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productId", entry.getKey());
            row.put("name", product == null ? entry.getKey() : product.getName());
            row.put("quantity", entry.getValue());
            row.put("revenue", roundMoney(revenueMap.getOrDefault(entry.getKey(), 0.0)));
            row.put("state", product == null ? null : product.getState());
            rows.add(row);
        }
        rows.sort((left, right) -> Double.compare(((Number) right.get("revenue")).doubleValue(),
                ((Number) left.get("revenue")).doubleValue()));
        if (rows.size() > 5) {
            return new ArrayList<>(rows.subList(0, 5));
        }
        return rows;
    }

    private Map<String, Object> buildTicketBreakdown(List<AfterSaleTicket> tickets) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("pending", countTicketsByStatus(tickets, "待处理"));
        stats.put("processing", countTicketsByStatus(tickets, "处理中"));
        stats.put("resolved", countTicketsByStatus(tickets, "已解决"));
        stats.put("closed", countTicketsByStatus(tickets, "已关闭"));
        stats.put("refund", safeList(tickets).stream().filter(item -> item != null && "退款问题".equals(item.getType())).count());
        stats.put("complaint", safeList(tickets).stream().filter(item -> item != null && "投诉反馈".equals(item.getType())).count());
        return stats;
    }

    private List<OperationAuditLog> limitLogs(List<OperationAuditLog> logs, int limit) {
        List<OperationAuditLog> list = new ArrayList<>(safeList(logs));
        list.sort(Comparator.comparing((OperationAuditLog item) -> item.getCreatedTime() == null ? LocalDateTime.MIN : item.getCreatedTime()).reversed());
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String describeOrderState(Integer state) {
        if (state == null) {
            return "未知";
        }
        switch (state) {
            case -3:
                return "已退款";
            case -2:
                return "退款中";
            case -1:
                return "待支付";
            case 0:
                return "已支付";
            case 1:
                return "配送中";
            case 2:
                return "已完成";
            case 3:
                return "待骑手接单";
            case 4:
                return "备餐中";
            default:
                return "状态" + state;
        }
    }

    private List<OperationPermission> buildDefaultPermissions(LocalDateTime now) {
        List<OperationPermission> defaults = new ArrayList<>();
        defaults.add(permission("admin", "admin.menu.goods", "商品治理", TYPE_MENU, "admin", now));
        defaults.add(permission("admin", "admin.menu.order", "订单总览", TYPE_MENU, "admin", now));
        defaults.add(permission("admin", "admin.menu.category", "分类管理", TYPE_MENU, "admin", now));
        defaults.add(permission("admin", "admin.menu.afterSale", "售后工单", TYPE_MENU, "admin", now));
        defaults.add(permission("admin", "admin.menu.user", "用户管理", TYPE_MENU, "admin", now));
        defaults.add(permission("admin", "admin.menu.ops", "治理与经营", TYPE_MENU, "admin", now));
        defaults.add(permission("admin", "admin.action.dashboard.view", "查看平台看板", TYPE_ACTION, "admin", now));
        defaults.add(permission("admin", "admin.action.permission.manage", "维护权限配置", TYPE_ACTION, "admin", now));
        defaults.add(permission("admin", "admin.action.user.view", "查看用户列表", TYPE_ACTION, "admin", now));
        defaults.add(permission("admin", "admin.action.user.disable", "启停用户账号", TYPE_ACTION, "admin", now));
        defaults.add(permission("admin", "admin.action.product.audit", "审核商品", TYPE_ACTION, "admin", now));
        defaults.add(permission("admin", "admin.action.afterSale.handle", "处理售后工单", TYPE_ACTION, "admin", now));
        defaults.add(permission("admin", "admin.action.audit.view", "查看审计日志", TYPE_ACTION, "admin", now));

        defaults.add(permission("mer", "merchant.menu.goods", "商品管理", TYPE_MENU, "merchant", now));
        defaults.add(permission("mer", "merchant.menu.order", "订单处理", TYPE_MENU, "merchant", now));
        defaults.add(permission("mer", "merchant.menu.afterSale", "售后处理", TYPE_MENU, "merchant", now));
        defaults.add(permission("mer", "merchant.menu.store", "门店资料", TYPE_MENU, "merchant", now));
        defaults.add(permission("mer", "merchant.menu.info", "账号信息", TYPE_MENU, "merchant", now));
        defaults.add(permission("mer", "merchant.menu.ops", "经营分析", TYPE_MENU, "merchant", now));
        defaults.add(permission("mer", "merchant.action.dashboard.view", "查看经营看板", TYPE_ACTION, "merchant", now));
        defaults.add(permission("mer", "merchant.action.report.export", "导出经营报表", TYPE_ACTION, "merchant", now));
        defaults.add(permission("mer", "merchant.action.afterSale.handle", "处理售后工单", TYPE_ACTION, "merchant", now));
        defaults.add(permission("mer", "merchant.action.store.manage", "维护门店资料", TYPE_ACTION, "merchant", now));
        defaults.add(permission("mer", "merchant.action.audit.view", "查看操作日志", TYPE_ACTION, "merchant", now));
        return defaults;
    }

    private OperationPermission permission(String roleCode, String key, String name, String type, String scope, LocalDateTime now) {
        return new OperationPermission(roleCode, key, name, type, scope, 1, now);
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private static class OrderBundle {
        private String orderId;
        private String customerId;
        private String merchantId;
        private Integer state;
        private LocalDateTime time;
        private LocalDateTime payTime;
        private double totalAmount;
        private int totalQuantity;
        private final Map<String, Integer> productQuantities = new LinkedHashMap<>();
        private final Map<String, Double> productRevenue = new LinkedHashMap<>();
    }

    private static class TrendMetrics {
        private int orders;
        private double gmv;
    }
}
