package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.ops.OperationAuditLog;
import com.DB.DBmarket.pojo.utils.CurrentUser;

import java.util.List;
import java.util.Map;

public interface OperationsService {
    Map<String, Object> getCurrentPermissionSnapshot(CurrentUser currentUser);

    Map<String, Object> listPermissionMatrix(CurrentUser currentUser);

    void updatePermission(CurrentUser currentUser, String roleCode, String permissionKey, Integer enabled);

    boolean hasPermission(CurrentUser currentUser, String permissionKey);

    Map<String, Object> getAdminDashboard(CurrentUser currentUser);

    Map<String, Object> getMerchantDashboard(CurrentUser currentUser);

    Map<String, Object> exportMerchantDashboard(CurrentUser currentUser);

    List<OperationAuditLog> listAuditLogs(CurrentUser currentUser, String scope, String actionType, String keyword);

    void recordAudit(CurrentUser currentUser, String actionType, String targetType, String targetId, String targetName, String detail, String result);
}
