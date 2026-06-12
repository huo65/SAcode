package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.afterSale.AfterSaleTicketView;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.AfterSaleService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequestMapping("/afterSale")
public class AfterSaleController {
    @Resource
    private AfterSaleService afterSaleService;

    @PostMapping("/create")
    public Result create(@RequestBody Map<String, String> request) {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            AfterSaleTicketView ticket = afterSaleService.createTicket(
                    currentUser,
                    request.get("orderId"),
                    request.get("type"),
                    request.get("content")
            );
            return Result.success(ticket, "After-sale ticket created.");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String scope,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String type) {
        CurrentUser currentUser = CurrentUserHolder.require();
        List<AfterSaleTicketView> tickets = afterSaleService.listTickets(currentUser, scope, status, type);
        Map<String, Object> data = new HashMap<>();
        data.put("ticketList", tickets);
        return Result.success(data);
    }

    @PostMapping("/update")
    public Result update(@RequestBody Map<String, String> request) {
        CurrentUser currentUser = CurrentUserHolder.require();
        try {
            AfterSaleTicketView ticket = afterSaleService.updateTicket(
                    currentUser,
                    request.get("id"),
                    request.get("status"),
                    request.get("handlerNote")
            );
            return Result.success(ticket, "After-sale ticket updated.");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result stats(@RequestParam(required = false) String scope) {
        CurrentUser currentUser = CurrentUserHolder.require();
        Map<String, Object> data = new HashMap<>();
        data.put("stats", afterSaleService.getStats(currentUser, scope));
        return Result.success(data);
    }
}
