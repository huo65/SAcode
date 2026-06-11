package com.DB.DBmarket.pojo.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayParmBody {
    private String id;
    private  ArrayList<ArrayList<Integer>>  price_list;
    private  ArrayList<String> orderIdList;
}
