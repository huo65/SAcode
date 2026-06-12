package com.DB.DBmarket.pojo.ops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationAuditLog {
    private String id;
    private String actorId;
    private String actorName;
    private String actorType;
    private String actionType;
    private String targetType;
    private String targetId;
    private String targetName;
    private String detail;
    private String result;
    private LocalDateTime createdTime;
}
