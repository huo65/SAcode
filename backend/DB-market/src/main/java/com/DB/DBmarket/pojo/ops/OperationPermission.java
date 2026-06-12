package com.DB.DBmarket.pojo.ops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationPermission {
    private String roleCode;
    private String permissionKey;
    private String permissionName;
    private String permissionType;
    private String scopeCode;
    private Integer enabled;
    private LocalDateTime updatedTime;
}
