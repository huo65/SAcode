package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    //地址id
    private String addrId;
    //用户id
    private  String usr;
    //地址位置描述
    private  String location;
}
