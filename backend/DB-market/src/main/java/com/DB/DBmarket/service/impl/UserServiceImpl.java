package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;


    @Override
    public User login(User user) {

        return userMapper.getByNameAndPassword(user);
    }

    @Override
    public Boolean register(User user) {
        if (userMapper.findByName(user.getName())!=null){
            return null;
        }else {
            Integer lastId;
            if (userMapper.getLastId( )== null){
                lastId = 1;
            }else {
                lastId = Integer.parseInt(userMapper.getLastId()) + 1;
            }
            String newId =  String.valueOf(lastId);
            user.setId(newId);
            user.setBalance(200);
            userMapper.insertUser(user);
            return true;
        }
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public List<Address> getUserAddress(String id) {
        return userMapper.getAddressById(id);
    }



    @Override
    public User getInfo(String id) {
        User user= userMapper.getInfo(id);
        user.setBalance(null);
        return user;
    }
}
