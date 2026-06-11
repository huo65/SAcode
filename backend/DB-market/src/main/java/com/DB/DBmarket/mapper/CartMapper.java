package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.Cart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartMapper {
    @Select("select * from cart where cus = #{id} ")
    List<Cart> getCart(String id);

    @Delete("delete from cart where cus = #{cus}")
    void deleteCartByCus(String cus);

    @Insert("insert into cart(cus, prod, number) values(#{cus}, #{prod}, #{number})")
    void addCart(Cart cart);
}
