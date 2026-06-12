package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.utils.OrderInfoReturn;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderInfoMapper {

    @Select("SELECT * FROM order_info WHERE id = #{orderId}")
    OrderInfo getOrderById(@Param("orderId") String orderId);

    @Update("UPDATE order_info SET state = #{newState}, time = #{updateTime}, complain = #{_complain}, complain_reason = #{complainReason}, refund_reason = #{refundReason} WHERE id = #{orderId}")
    int updateOrderState1(@Param("orderId") String orderId, @Param("newState") int newState, @Param("updateTime") String updateTime, @Param("_complain") String _complain, @Param("complainReason") String complainReason, @Param("refundReason") String refundReason);

    @Update("UPDATE order_info SET state = #{newState}, time = #{updateTime} WHERE id = #{orderId}")
    int updateOrderState2(@Param("orderId") String orderId, @Param("newState") int newState, @Param("updateTime") String updateTime);

    @Update("<script>UPDATE order_info SET state = #{newState}, time = #{updateTime}<if test='driverId != null'>, driver_id = #{driverId}</if><if test='payTime != null'>, pay_time = #{payTime}</if><if test='complain != null'>, complain = #{complain}</if><if test='complainReason != null'>, complain_reason = #{complainReason}</if><if test='refundReason != null'>, refund_reason = #{refundReason}</if> WHERE id = #{orderId}</script>")
    int updateOrderState(@Param("orderId") String orderId, @Param("newState") int newState, @Param("updateTime") String updateTime, @Param("driverId") String driverId, @Param("payTime") String payTime, @Param("complain") String complain, @Param("complainReason") String complainReason, @Param("refundReason") String refundReason);

    @Update("<script>UPDATE order_info SET state = #{newState}, time = #{updateTime}, driver_id = NULL<if test='payTime != null'>, pay_time = #{payTime}</if><if test='complain != null'>, complain = #{complain}</if><if test='complainReason != null'>, complain_reason = #{complainReason}</if><if test='refundReason != null'>, refund_reason = #{refundReason}</if> WHERE id = #{orderId}</script>")
    int updateOrderStateAndClearDriver(@Param("orderId") String orderId, @Param("newState") int newState, @Param("updateTime") String updateTime, @Param("payTime") String payTime, @Param("complain") String complain, @Param("complainReason") String complainReason, @Param("refundReason") String refundReason);

    @Select("SELECT * FROM order_info")
    List<OrderInfo> getAllOrders();

    @Select("SELECT * FROM order_info WHERE state = #{state}")
    List<OrderInfo> getOrdersByState(@Param("state") int state);

    @Insert("insert into order_info(id,cus,mer,prod,prod_num,time,deli_addr,rec_addr,state,account,driver_id,remark,expected_delivery_time,pay_time) values (#{id},#{cus},#{mer},#{prod},#{prodNum},#{time},#{deliAddr},#{recAddr},#{state},#{account},#{driverId},#{remark},#{expectedDeliveryTime},#{payTime})")
        void addOrder(OrderInfo orderInfo);
    @Update("update order_info set state=0 where id=#{order_id}")
        void payForOrder(String order_id);
    @Select("SELECT COUNT(*) FROM market.order_info WHERE prod=#{id} AND state=-3 ")
    Integer getRefundNumById(String id);
    @Select("SELECT COUNT(*) FROM market.order_info WHERE prod=#{id}")
    Double getOrderSumById(String id);
    @Select("SELECT COUNT(*) FROM market.order_info WHERE prod=#{id} AND complain='1'")
    Integer getCompNumById(String id);

    @Select("select * from order_info where id = #{order_id}")
    List<OrderInfo> getOrdersById(String order_id);






    class OrderInfoSqlProvider {

        public String getCusOrderSql(Map<String, Object> params) {
            String id = (String) params.get("id");
            Integer state = (Integer) params.get("state");
            Integer timeOrder = (Integer) params.get("timeOrder");

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM market.order_info WHERE cus = #{id}");
            if (state != null) {
                sql.append(" AND state = #{state}");
            }
            if (timeOrder != null) {
                sql.append(" ORDER BY time ");
                if (timeOrder == 0) {
                    sql.append("ASC");
                } else {
                    sql.append("DESC");
                }
            }
            return sql.toString();
        }

        public String getMerOrderSql(Map<String, Object> params) {
            String id = (String) params.get("id");
            Integer state = (Integer) params.get("state");
            Integer timeOrder = (Integer) params.get("timeOrder");

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM market.order_info WHERE mer = #{id}");
            if (state != null) {
                sql.append(" AND state = #{state}");
            }
            if (timeOrder != null) {
                sql.append(" ORDER BY time ");
                if (timeOrder == 0) {
                    sql.append("ASC");
                } else {
                    sql.append("DESC");
                }
            }
            return sql.toString();
        }

        public String getAllOrderSql(Map<String, Object> params) {
            String id = (String) params.get("id");
            Integer state = (Integer) params.get("state");
            Integer timeOrder = (Integer) params.get("timeOrder");

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM market.order_info");
            if (state != null) {
                sql.append(" WHERE state = #{state}");
            }
            if (timeOrder != null) {
                sql.append(" ORDER BY time ");
                if (timeOrder == 0) {
                    sql.append("ASC");
                } else {
                    sql.append("DESC");
                }
            }
            return sql.toString();
        }

        public String getDriverOrderSql(Map<String, Object> params) {
            Integer state = (Integer) params.get("state");
            Integer timeOrder = (Integer) params.get("timeOrder");

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM market.order_info WHERE (state = 3 OR driver_id = #{id})");
            if (state != null) {
                sql.append(" AND state = #{state}");
            }
            if (timeOrder != null) {
                sql.append(" ORDER BY time ");
                if (timeOrder == 0) {
                    sql.append("ASC");
                } else {
                    sql.append("DESC");
                }
            }
            return sql.toString();
        }
    }

    @SelectProvider(type = OrderInfoSqlProvider.class, method = "getCusOrderSql")
    ArrayList<OrderInfo> getCusOrder(@Param("id") String id, @Param("state") Integer state, @Param("timeOrder") Integer timeOrder);

    @SelectProvider(type = OrderInfoSqlProvider.class, method = "getMerOrderSql")
    ArrayList<OrderInfo> getMerOrder(@Param("id") String id, @Param("state") Integer state, @Param("timeOrder") Integer timeOrder);
    @SelectProvider(type = OrderInfoSqlProvider.class, method = "getAllOrderSql")
    ArrayList<OrderInfo> getAllOrders_518(@Param("id") String id, @Param("state") Integer state, @Param("timeOrder") Integer timeOrder);

    @SelectProvider(type = OrderInfoSqlProvider.class, method = "getDriverOrderSql")
    ArrayList<OrderInfo> getDriverOrder(@Param("id") String id, @Param("state") Integer state, @Param("timeOrder") Integer timeOrder);

    @Select("select state from market.order_info where id=#{order_id}")
    List<Integer> getOrderState(String order_id);

    @Select("select COALESCE(sum(account), 0) from market.order_info where id=#{orderId}")
    Integer getOrderAccount(String orderId);
}
