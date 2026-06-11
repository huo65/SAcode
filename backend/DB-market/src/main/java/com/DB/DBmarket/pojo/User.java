package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    //用户唯一标识
    private String id;
    //用户类型
    private String type;
    //用户昵称
    private String name;
    //头像
    private String portrait;
    //密码
    private String password;
    //手机号
    private String phone;
    //用户余额
    private  Integer balance;
    //描述
    private String description;
}
