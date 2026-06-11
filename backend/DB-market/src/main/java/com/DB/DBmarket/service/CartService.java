package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.CartList;
import com.DB.DBmarket.pojo.CartUpdateRequest;
import com.DB.DBmarket.pojo.OrderCartRequest;

import java.util.List;

public interface CartService {
    //修改商品数量
    boolean updateCart(CartUpdateRequest cartUpdateRequest);
    //获取购物车
    CartList getCart(String id);

    List<String> makeOrder(OrderCartRequest orderCartRequest);

    List<String> makeOrder_5_11(OrderCartRequest orderCartRequest);
}
