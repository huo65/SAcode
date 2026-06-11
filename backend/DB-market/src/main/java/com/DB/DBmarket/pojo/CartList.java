package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CartList {
    private List<CartInfo> cartList;
    public CartList(){
        this.cartList = new ArrayList<>();
    }
}