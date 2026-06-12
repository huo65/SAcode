package com.DB.DBmarket.pojo.afterSale;

import lombok.Data;

@Data
public class AfterSaleTicketView extends AfterSaleTicket {
    private String customerName;
    private String merchantName;
    private String handlerName;
    private Integer orderState;
    private Integer orderAmount;
    private String orderTime;
    private String receiveAddress;
    private String deliveryAddress;
}
