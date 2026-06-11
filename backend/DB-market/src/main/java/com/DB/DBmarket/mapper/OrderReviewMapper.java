package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.OrderReview;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderReviewMapper {

    @Insert("INSERT INTO order_review(order_id, cus, mer, score, content, created_time) VALUES (#{orderId}, #{cus}, #{mer}, #{score}, #{content}, #{createdTime})")
    int insert(OrderReview orderReview);

    @Select("SELECT * FROM order_review WHERE order_id = #{orderId}")
    OrderReview getByOrderId(@Param("orderId") String orderId);

    @Select("SELECT * FROM order_review WHERE mer = #{merchantId} ORDER BY created_time DESC")
    List<OrderReview> listByMerchantId(@Param("merchantId") String merchantId);
}
