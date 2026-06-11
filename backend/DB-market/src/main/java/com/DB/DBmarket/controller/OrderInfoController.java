package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.pojo.utils.OrderList;
import com.DB.DBmarket.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderInfoController {
    @Resource
    private OrderInfoService orderInfoService;
    @PostMapping("/get")
    public Result getOrderInfo(
            @RequestParam String usrId,
            @RequestParam(required = false) Integer state,
            @RequestParam(required = false) Integer timeOrder
    ) {
        if (state != null && (state < -3 || state > 3)) {
            return Result.error("Invalid value for state parameter");
        }
        if (timeOrder != null && (timeOrder != 0 && timeOrder != 1)) {
            return Result.error("Invalid value for timeOrder parameter");
        }

        CurrentUser currentUser = CurrentUserHolder.require();
        String targetId = currentUser.isAdmin() && usrId != null ? usrId : currentUser.getId();
        OrderList orderList = orderInfoService.getOrderInfo(targetId, state, timeOrder);
        return Result.success(orderList);
    }


    @PostMapping("/update")
    public Result updateOrderState(@RequestBody Map<String, Object> request) {
        //判断当订单状态由-2->-3（退货退款）时，调用退款的api:     src/main/java/com/DB/DBmarket/controller/AliPayController.java--refund
        String id = (String) request.get("id");
        Object rawState = request.containsKey("targetState") ? request.get("targetState") : request.get("state");
        int state = rawState instanceof Integer ? (Integer) rawState : Integer.parseInt(String.valueOf(rawState));
        String complain = (String) request.get("complain");
        String complainReason = (String) request.get("complain_reason");
        if (complainReason == null) complainReason = (String) request.get("complainReason");
        String refundReason = (String) request.get("refund_reason");
        if (refundReason == null) refundReason = (String) request.get("refundReason");
        if (state < -3 || state > 3) {
            return Result.error("Invalid order state!");
        }
        try {
            OrderInfo updatedOrder = orderInfoService.transitionOrder(CurrentUserHolder.require(), id, state, complain, complainReason, refundReason);
            if (updatedOrder != null) {
                return Result.success(updatedOrder, "Order state updated successfully!");
            } else {
                return Result.error("Failed to update order state!");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }


    @GetMapping("/filter")
    public Result listByOrderState(@RequestParam(required = false) Integer state) {
        List<OrderInfo> orders;
        if (state != null) {
            if (state >= -3 && state <= 3) {
                orders = orderInfoService.getOrdersByState(state);
                return Result.success(orders, "Orders retrieved successfully based on order state!");
            } else {
                return Result.error("Invalid order state!");
            }
        } else {
            orders = orderInfoService.getAllOrders();
            return Result.success(orders, "No 'state' parameter provided. Returning all orders by default!");
        }
    }
}
