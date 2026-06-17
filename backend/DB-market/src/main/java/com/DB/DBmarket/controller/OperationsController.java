package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.ops.OperationAuditLog;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.pojo.wallet.WalletTransaction;
import com.DB.DBmarket.service.OperationsService;
import com.DB.DBmarket.service.WalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ops")
public class OperationsController {
    @Resource(name = "OperationsService")
    private OperationsService operationsService;
    @Resource(name = "WalletService")
    private WalletService walletService;

    @GetMapping("/me")
    public Result me() {
        CurrentUser currentUser = CurrentUserHolder.require();
        return Result.success(operationsService.getCurrentPermissionSnapshot(currentUser));
    }

    @GetMapping("/permission/list")
    public Result listPermissions() {
        CurrentUser currentUser = CurrentUserHolder.require();
        return Result.success(operationsService.listPermissionMatrix(currentUser));
    }

    @PostMapping("/permission/update")
    public Result updatePermission(@RequestBody Map<String, Object> request) {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            String roleCode = request.get("roleCode") == null ? null : String.valueOf(request.get("roleCode"));
            String permissionKey = request.get("permissionKey") == null ? null : String.valueOf(request.get("permissionKey"));
            Integer enabled = request.get("enabled") == null ? null : Integer.parseInt(String.valueOf(request.get("enabled")));
            operationsService.updatePermission(currentUser, roleCode, permissionKey, enabled);
            return Result.success("Permission updated successfully.");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/dashboard")
    public Result adminDashboard() {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            return Result.success(operationsService.getAdminDashboard(currentUser));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/dashboard")
    public Result merchantDashboard() {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            return Result.success(operationsService.getMerchantDashboard(currentUser));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/export")
    public Result exportMerchantDashboard() {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            return Result.success(operationsService.exportMerchantDashboard(currentUser));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/audit/list")
    public Result listAuditLogs(@RequestParam(required = false) String scope,
                                 @RequestParam(required = false) String actionType,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer pageSize) {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            String resolvedActionType = "all".equalsIgnoreCase(String.valueOf(actionType)) ? null : actionType;
            List<OperationAuditLog> logs = operationsService.listAuditLogs(currentUser, scope, resolvedActionType, keyword);
            int safePage = page == null || page < 1 ? 1 : page;
            int safePageSize = pageSize == null || pageSize < 1 ? logs.size() : pageSize;
            int from = Math.min((safePage - 1) * safePageSize, logs.size());
            int to = Math.min(from + safePageSize, logs.size());
            List<OperationAuditLog> pageRows = logs.subList(from, to);
            Map<String, Object> data = new HashMap<>();
            data.put("auditLogs", pageRows);
            data.put("list", pageRows);
            data.put("total", logs.size());
            data.put("page", safePage);
            data.put("pageSize", safePageSize);
            data.put("stats", buildAuditStats(logs));
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/wallet/transactions")
    public Result listWalletTransactions(@RequestParam(required = false) String userId,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) Integer limit,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer pageSize,
                                         @RequestParam(required = false) String keyword) {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) {
            return Result.error("Only admin can view wallet transactions.");
        }
        if (!operationsService.hasPermission(currentUser, "admin.action.wallet.view")) {
            return Result.error("Admin permission denied: admin.action.wallet.view");
        }
        try {
            int safePage = page == null || page <= 0 ? 1 : page;
            int safePageSize = pageSize == null || pageSize <= 0 ? 20 : Math.min(pageSize, 100);
            int queryLimit = limit == null || limit <= 0 ? Math.max(200, safePage * safePageSize) : limit;
            List<WalletTransaction> source = walletService.listTransactions(currentUser, userId, type, queryLimit);
            List<WalletTransaction> filtered = filterWalletTransactions(source, keyword);
            int total = filtered.size();
            int fromIndex = Math.min((safePage - 1) * safePageSize, total);
            int toIndex = Math.min(fromIndex + safePageSize, total);
            List<WalletTransaction> pageList = filtered.subList(fromIndex, toIndex);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("transactions", pageList);
            data.put("list", pageList);
            data.put("total", total);
            data.put("page", safePage);
            data.put("pageSize", safePageSize);
            data.put("platformStats", buildWalletStats(filtered));
            data.put("typeDistribution", buildWalletTypeDistribution(filtered));
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private Map<String, Object> buildAuditStats(List<OperationAuditLog> rows) {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        int totalToday = 0;
        int permissionChanges = 0;
        int afterSaleActions = 0;
        for (OperationAuditLog row : rows) {
            if (row.getCreatedTime() != null && today.equals(row.getCreatedTime().toLocalDate())) {
                totalToday++;
            }
            if ("PERMISSION_UPDATE".equals(row.getActionType())) {
                permissionChanges++;
            }
            if ("AFTER_SALE_UPDATE".equals(row.getActionType())) {
                afterSaleActions++;
            }
        }
        stats.put("totalToday", totalToday);
        stats.put("totalAll", rows.size());
        stats.put("permissionChanges", permissionChanges);
        stats.put("afterSaleActions", afterSaleActions);
        return stats;
    }

    private List<WalletTransaction> filterWalletTransactions(List<WalletTransaction> source, String keyword) {
        if (source == null) {
            return new ArrayList<>();
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return source;
        }
        String normalized = keyword.trim().toLowerCase();
        return source.stream()
                .filter(row -> contains(row.getId(), normalized)
                        || contains(row.getUserId(), normalized)
                        || contains(row.getUserName(), normalized)
                        || contains(row.getRelatedOrderId(), normalized)
                        || contains(row.getRemark(), normalized))
                .collect(Collectors.toList());
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private Map<String, Object> buildWalletStats(List<WalletTransaction> rows) {
        Map<String, Object> stats = new LinkedHashMap<>();
        int recharge = 0;
        int pay = 0;
        int refund = 0;
        for (WalletTransaction row : rows) {
            int amount = row.getAmount() == null ? 0 : row.getAmount();
            if ("RECHARGE".equals(row.getType())) {
                recharge += Math.abs(amount);
            } else if ("PAY".equals(row.getType())) {
                pay += Math.abs(amount);
            } else if ("REFUND".equals(row.getType())) {
                refund += Math.abs(amount);
            }
        }
        stats.put("totalRecharge", recharge);
        stats.put("totalPay", pay);
        stats.put("totalRefund", refund);
        stats.put("netInflow", recharge - refund);
        return stats;
    }

    private List<Map<String, Object>> buildWalletTypeDistribution(List<WalletTransaction> rows) {
        Map<String, int[]> buckets = new LinkedHashMap<>();
        buckets.put("RECHARGE", new int[]{0, 0});
        buckets.put("PAY", new int[]{0, 0});
        buckets.put("REFUND", new int[]{0, 0});
        int totalAmount = 0;
        for (WalletTransaction row : rows) {
            if (!buckets.containsKey(row.getType())) {
                continue;
            }
            int amount = Math.abs(row.getAmount() == null ? 0 : row.getAmount());
            buckets.get(row.getType())[0] += amount;
            buckets.get(row.getType())[1] += 1;
            totalAmount += amount;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> names = new HashMap<>();
        names.put("RECHARGE", "充值");
        names.put("PAY", "支付");
        names.put("REFUND", "退款");
        Map<String, String> tones = new HashMap<>();
        tones.put("RECHARGE", "primary");
        tones.put("PAY", "warning");
        tones.put("REFUND", "info");
        for (Map.Entry<String, int[]> entry : buckets.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", entry.getKey());
            item.put("typeName", names.get(entry.getKey()));
            item.put("amount", entry.getValue()[0]);
            item.put("count", entry.getValue()[1]);
            item.put("percent", totalAmount == 0 ? 0 : Math.round(entry.getValue()[0] * 100.0 / totalAmount));
            item.put("tone", tones.get(entry.getKey()));
            result.add(item);
        }
        return result;
    }
}
