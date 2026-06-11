package com.DB.DBmarket.pojo.utils;

import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoReturn {
    private OrderInfo orderInfo;
    //发货地址字符串
    private String delivery;
    //收获地址字符串
    private String receive;
    //购买商品的数组
    private List<ProductReturn> productList;

    private String cusName;
    private String merName;
    //private String img;//商品第一张图片
    private List<String> imgList;//商品第一张图片
    private Boolean reviewed;
    private OrderReview review;
}
