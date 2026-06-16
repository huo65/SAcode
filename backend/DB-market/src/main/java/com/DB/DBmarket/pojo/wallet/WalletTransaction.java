package com.DB.DBmarket.pojo.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransaction {
    private String id;
    private String userId;
    private String userName;
    private String type;
    private Integer amount;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String relatedOrderId;
    private String remark;
    private String actorId;
    private String actorName;
    private String actorType;
    private LocalDateTime createdTime;
}
