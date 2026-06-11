package com.DB.DBmarket.controller;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.pojo.*;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.service.CartService;
import com.DB.DBmarket.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/cart")
public class CartController {
    @Resource
    CartService cartService;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private OrderInfoService orderInfoService;
    //获取购物车
    @GetMapping("/getCart")
    public Result getCart(@RequestParam("id") String id){
        CurrentUser currentUser = CurrentUserHolder.require();
        String targetId = currentUser.isAdmin() ? id : currentUser.getId();
        log.info("getCart id:{}",targetId);
        CartList cartList = cartService.getCart(targetId);
        if(cartList != null){
            return Result.success(cartList);
        }
        else{
            return Result.error("获取购物车失败");
        }
    }

    //修改购物车
    @PostMapping("/update")
    public Result updateCart(@RequestBody CartUpdateRequest cartUpdateRequest){
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isCustomer() && !currentUser.isAdmin()) return Result.error("Only customers can update cart.");
        if (!currentUser.isAdmin()) cartUpdateRequest.setCus(currentUser.getId());
        log.info("CartUpdateRequest:{}",cartUpdateRequest);
        if(cartService.updateCart(cartUpdateRequest)){
            return Result.success("修改购物车成功");
        }
        return Result.error("修改购物车失败");
    }

    @PostMapping("/makeOrder")
    public Result makeOrder(@RequestBody OrderCartRequest orderCartRequest){
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isCustomer() && !currentUser.isAdmin()) return Result.error("Only customers can submit cart orders.");
        if (!currentUser.isAdmin()) {
            for (OrderInfoCart item : orderCartRequest.getOrderList()) {
                item.setCus(currentUser.getId());
            }
        }
        log.info("orderCartRequest:{}",orderCartRequest);
        //List<String> orderIdList = cartService.makeOrder(orderCartRequest);
        //如果要给返回的List命名，使用HashMap<String,Object> data=new HashMap<>()，然后data.put("order_id_list",orderIdList)；封装
        List<String> orderIdList = cartService.makeOrder_5_11(orderCartRequest);
        if(orderIdList != null){
            return Result.success(orderIdList,"下单成功");
        }
        else{
            return Result.error("下单失败");
        }
    }
}
