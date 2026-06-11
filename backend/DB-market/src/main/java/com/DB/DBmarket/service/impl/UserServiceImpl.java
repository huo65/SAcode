package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.utils.PasswordUtil;
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
        User stored = userMapper.getByName(user.getName());
        if (stored == null || !PasswordUtil.matches(user.getPassword(), stored.getPassword())) {
            return null;
        }
        if (stored.getDisabled() != null && stored.getDisabled() == 1) {
            return null;
        }
        if (!PasswordUtil.isHashed(stored.getPassword())) {
            User migrated = new User();
            migrated.setId(stored.getId());
            migrated.setPassword(PasswordUtil.hash(user.getPassword()));
            userMapper.update(migrated);
            stored.setPassword(migrated.getPassword());
        }
        stored.setPassword(null);
        return stored;
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
            user.setPassword(PasswordUtil.hash(user.getPassword()));
            if (user.getDisabled() == null) user.setDisabled(0);
            userMapper.insertUser(user);
            return true;
        }
    }

    @Override
    public void update(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        }
        userMapper.update(user);
    }

    @Override
    public List<Address> getUserAddress(String id) {
        return userMapper.getAddressById(id);
    }



    @Override
    public User getInfo(String id) {
        User user= userMapper.getInfo(id);
        if (user != null) {
            user.setBalance(null);
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public List<User> listUsers(String type) {
        List<User> users = userMapper.listUsers(type);
        for (User user : users) {
            user.setPassword(null);
        }
        return users;
    }

    @Override
    public boolean updateDisabled(String id, Integer disabled) {
        return userMapper.updateDisabled(id, disabled) > 0;
    }
}
