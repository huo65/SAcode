package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.ops.OperationAuditLog;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.OperationsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ops")
public class OperationsController {
    @Resource(name = "OperationsService")
    private OperationsService operationsService;

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
                                @RequestParam(required = false) String keyword) {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            List<OperationAuditLog> logs = operationsService.listAuditLogs(currentUser, scope, actionType, keyword);
            Map<String, Object> data = new HashMap<>();
            data.put("auditLogs", logs);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
