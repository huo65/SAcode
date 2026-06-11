package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.User;

import java.util.List;

public interface UserService {
    User getInfo(String id);

    //登录
    User login(User user);

    //注册
    Boolean register(User user);

    //修改用户信息
    void update(User user);

    List<Address> getUserAddress(String id);

    List<User> listUsers(String type);

    boolean updateDisabled(String id, Integer disabled);
}
