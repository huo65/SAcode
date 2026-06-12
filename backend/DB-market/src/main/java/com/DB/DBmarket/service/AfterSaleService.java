package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.afterSale.AfterSaleTicketView;
import com.DB.DBmarket.pojo.utils.CurrentUser;

import java.util.List;
import java.util.Map;

public interface AfterSaleService {
    AfterSaleTicketView createTicket(CurrentUser currentUser, String orderId, String type, String content);

    List<AfterSaleTicketView> listTickets(CurrentUser currentUser, String scope, String status, String type);

    AfterSaleTicketView updateTicket(CurrentUser currentUser, String id, String status, String handlerNote);

    Map<String, Object> getStats(CurrentUser currentUser, String scope);
}
