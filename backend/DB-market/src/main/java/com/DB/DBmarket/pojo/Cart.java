package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    //用户id
    private  String cus;
    //商品id
    private String prod;
    //商品数量
    private  Integer number;
}
