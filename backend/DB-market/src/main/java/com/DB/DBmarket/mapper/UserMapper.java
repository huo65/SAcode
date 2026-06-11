package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import java.util.List;


@Mapper
public interface UserMapper {
    @Update("update user set balance=#{s} where id=#{id}")
    void refundOrPay(String id,double s);
    @Select("select balance from market.user where id=#{id}")
    double getBalance(String id);
    @Select("select * from market.user where id=#{id}")
    User getInfo(String id);

    //通过name和password获取用户
    @Select("select * from user where name = #{name} and password = #{password}")
    User getByNameAndPassword(User user);

    //注册新用户
    @Insert("insert into user(id, type, name, portrait, password, phone, balance) " +
            "VALUES (#{id},#{type}, #{name}, #{portrait}, #{password}, #{phone}, #{balance})")
    void insertUser(User user);

    //根据用户名查找用户
    @Select("select * from market.user where name=#{name}")
    User findByName(String name);

    //获得用户列表最后一个id
    @Select("select id from user order by id desc limit 1")
    String getLastId();

    //更新用户信息
    @UpdateProvider(type = UserMapper.UserProvider.class, method = "updateUser")
    void update(User user);

    //根据id获取用户地址
    @Select("select * from market.address where usr=#{id}")
    List<Address> getAddressById(String id);
//    @Select("select * from user where phone = #{phone} and password=#{password}")
//    User getByPhoneAndPassword(User user);
    @Select("select type from market.user where id=#{id};")
    String getUserType(String id);
    @Select("select name from user where id = #{mer}")
    String getNameById(String mer);
    @Select("select id from user where name = #{mer}")
    String getIdByName(String mer);

    class UserProvider {
        public static String updateUser(User user) {
            return new SQL(){
                {
                    UPDATE("user");
                    SET("id = #{id}");

                    if (user.getName() != null && !user.getName().isEmpty()) {
                        SET("name = #{name}");
                    }
                    if (user.getPortrait() != null && !user.getPortrait().isEmpty()) {
                        SET("portrait = #{portrait}");
                    }
                    if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                        SET("password = #{password}");
                    }
                    if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                        SET("phone = #{phone}");
                    }
                    if (user.getDescription() != null) {
                        SET("description = #{description}");
                    }
                    WHERE("id = #{id}");
                }
            }.toString();
        }
    }

}
