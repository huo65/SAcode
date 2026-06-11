package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Result;
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
        if (state != null && (state < -3 || state > 2)) {
            return Result.error("Invalid value for state parameter");
        }
        if (timeOrder != null && (timeOrder != 0 && timeOrder != 1)) {
            return Result.error("Invalid value for timeOrder parameter");
        }

        OrderList orderList = orderInfoService.getOrderInfo(usrId, state, timeOrder);
        return Result.success(orderList);
    }


    @PostMapping("/update")
    public Result updateOrderState(@RequestBody Map<String, Object> request) {
        //判断当订单状态由-2->-3（退货退款）时，调用退款的api:     src/main/java/com/DB/DBmarket/controller/AliPayController.java--refund
        String id = (String) request.get("id");
        int state = (int) request.get("state");
        String time = String.valueOf(LocalDateTime.now());
        String complain = (String) request.get("complain");
        String complainReason = (String) request.get("complain_reason");
        String refundReason = (String) request.get("refund_reason");
        if (state < -3 || state > 2) {
            return Result.error("Invalid order state!");
        }
        OrderInfo updatedOrder;
        // 3个参数都不传，情况特殊处理
        if (Objects.equals(complain, "") && Objects.equals(complainReason, "") && Objects.equals(refundReason, "")) {
            updatedOrder = orderInfoService.updateOrderState2(id, state, time);
        }
        else {
            if (Objects.equals(complain, "")) {
                updatedOrder = orderInfoService.updateOrderState1(id, state, time, "0", complainReason, refundReason);
            }
            else {
                updatedOrder = orderInfoService.updateOrderState1(id, state, time, complain, complainReason, refundReason);
            }
        }
        if (updatedOrder != null) {
            return Result.success(updatedOrder, "Order state updated successfully!");
        } else {
            return Result.error("Failed to update order state!");
        }
    }


    @GetMapping("/filter")
    public Result listByOrderState(@RequestParam(required = false) Integer state) {
        List<OrderInfo> orders;
        if (state != null) {
            if (state >= -3 && state <= 2) {
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
