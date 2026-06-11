package com.DB.DBmarket.pojo.utils;

import com.DB.DBmarket.pojo.OrderInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderList {
    private ArrayList<OrderInfoReturn> cusList;

    private ArrayList<OrderInfoReturn> merList;

    private ArrayList<OrderInfoReturn> allOrderList;
}
