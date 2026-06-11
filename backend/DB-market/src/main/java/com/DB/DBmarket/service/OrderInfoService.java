package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.OrderList;

import java.util.ArrayList;
import java.util.List;

public interface OrderInfoService {
    Boolean addOrder(OrderInfo orderInfo);

    Integer pay(String id, ArrayList<Integer> prince_list, String order_id);

    void payOrders(CurrentUser currentUser, List<String> orderIdList);

    OrderList getOrderInfo(String id, Integer state, Integer timeOrder);

    List<Product> getProductList(String productId);

    List<Integer> getProductNumberList(String productNumbers);

    // 接口重载
    OrderInfo updateOrderState1(String orderId, int newState, String updateTime, String  complain, String complainReason, String refundReason);

    OrderInfo updateOrderState2(String orderId, int newState, String updateTime);

    OrderInfo transitionOrder(CurrentUser currentUser, String orderId, int targetState, String complain, String complainReason, String refundReason);

    List<OrderInfo> getAllOrders();

    List<OrderInfo> getOrdersByState(int state);

    List<OrderInfo> getOrderByOrderId(String orderId);
}
