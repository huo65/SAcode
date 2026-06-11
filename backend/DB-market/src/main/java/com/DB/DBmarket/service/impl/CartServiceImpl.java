package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.*;
import com.DB.DBmarket.pojo.*;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.pojo.utils.StringUtil;
import com.DB.DBmarket.service.CartService;
import com.DB.DBmarket.service.OrderInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Resource
    CartMapper cartMapper;
    @Resource
    ProductMapper productMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    ProdImgMapper prodImgMapper;
    @Resource
    OrderInfoMapper orderInfoMapper;
    @Resource
    OrderInfoService orderInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCart(CartUpdateRequest cartUpdateRequest) {
        try{
            List<Cart> cartList = cartUpdateRequest.getCartList();
            cartMapper.deleteCartByCus(cartUpdateRequest.getCus());
            for(Cart cart : cartList){
                cart.setCus(cartUpdateRequest.getCus());
                cartMapper.addCart(cart);
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartList getCart(String id) {
        try{
            CartList cartList = new CartList();
            List<Cart> carts = cartMapper.getCart(id);
            if (carts.size() == 0) {
                return cartList;
            }
            for(Cart cart : carts){
                CartInfo cartInfo = new CartInfo();
                cartInfo.setId(cart.getProd());//商品id
                Product product = productMapper.getProductById(cart.getProd()).get(0);
                cartInfo.setName(product.getName());//商品名称
                cartInfo.setDescription(product.getDescription());//商品描述
                cartInfo.setPrice(product.getPrice());//商品价格
                cartInfo.setMer(product.getMer());//商品商家id
                cartInfo.setMerName(userMapper.getNameById(product.getMer()));//商品商家名称
                cartInfo.setCatName(product.getCatName());//商品分类
                cartInfo.setNumber(product.getNumber());//商品库存量
                cartInfo.setState(product.getState());//商品状态
                cartInfo.setFirstImage(prodImgMapper.getProdImg(product.getId()).get(0));//商品图片
                cartInfo.setNumberInCart(cart.getNumber());//商品在购物车数量
                cartList.getCartList().add(cartInfo);
            }
            return cartList;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> makeOrder(OrderCartRequest orderCartRequest) {
        try{
            List<OrderInfoCart> orderCarts = orderCartRequest.getOrderList();
            List<String> OrderIdList = new ArrayList<>();
            for(OrderInfoCart orderCart : orderCarts){
                List<String> prodList = StringUtil.parseStringToList(orderCart.getProd());
                //List<String> numberList = StringUtil.parseStringToList(orderCart.getProdNum());
                for(int i = 0; i < prodList.size(); i++){
                    OrderInfo orderInfo = new OrderInfo();
                    orderInfo.setId(RandomIdGenerator.getRandomId());
                    orderInfo.setCus(orderCart.getCus());
                    orderInfo.setMer(orderCart.getMer());
                    orderInfo.setProd(prodList.get(i));
                    //orderInfo.setProdNum(Integer.parseInt(numberList.get(i)));
                    orderInfo.setTime(LocalDateTime.now());
                    orderInfo.setDeliAddr(orderCart.getDeliAddr());
                    orderInfo.setRecAddr(orderCart.getRecAddr());
                    orderInfo.setState(1);
                    //orderInfo.setAccount(productMapper.getPrice(prodList.get(i)) * Integer.parseInt(numberList.get(i)));
                    orderInfoMapper.addOrder(orderInfo);
                    OrderIdList.add(orderInfo.getId());
                }
            }
            return OrderIdList;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> makeOrder_5_11(OrderCartRequest orderCartRequest) {
        List<String> orderIdList = new ArrayList<>();
        //用哈希表存储该是否下单过该商家的商品以及订单ID
        Map<String,String> merchantMap = new HashMap<>();
        for(OrderInfoCart orderInfoCart:orderCartRequest.getOrderList()){
            OrderInfo orderInfo = new OrderInfo();

            orderInfo.setMer(orderInfoCart.getMer());
            orderInfo.setCus(orderInfoCart.getCus());
            orderInfo.setProd(orderInfoCart.getProd());
            orderInfo.setAccount(orderInfoCart.getAccount());
            orderInfo.setDeliAddr(orderInfoCart.getDeliAddr());
            orderInfo.setRecAddr(orderInfoCart.getRecAddr());
            orderInfo.setState(orderInfoCart.getState());
            orderInfo.setTime(LocalDateTime.now());

            if(merchantMap.containsKey(orderInfoCart.getMer())){
                //如果下单过该商家的商品,则使用上一次的订单ID
                System.out.println("已经包含");
                System.out.println(orderInfoCart.getMer()+"id为"+merchantMap.get(orderInfoCart.getMer()));
                orderInfo.setId(merchantMap.get(orderInfoCart.getMer()));
            }else{
                //否则生成新的ID
                System.out.println(orderInfoCart.getMer());
                System.out.println("没有包含");
                String orderId = RandomIdGenerator.getRandomId();
                orderInfo.setId(orderId);
                merchantMap.put(orderInfoCart.getMer(),orderId);
            }
            orderInfo.setProdNum(orderInfoCart.getProdNum());

            orderInfoService.addOrder(orderInfo);
            System.out.println("加入数据库订单ID为"+orderInfo.getId());
            orderIdList.add(orderInfo.getId());
        }
        return orderIdList;
    }


}
