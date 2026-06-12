package com.DB.DBmarket.pojo.afterSale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AfterSaleTicket {
    private String id;
    private String orderId;
    private String customerId;
    private String merchantId;
    private String type;
    private String content;
    private String status;
    private String handlerId;
    private String handlerNote;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
