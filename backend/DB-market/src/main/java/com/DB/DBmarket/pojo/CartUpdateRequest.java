package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartUpdateRequest {
    String cus;
    List<Cart> cartList;
}
