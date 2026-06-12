package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.afterSale.AfterSaleTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AfterSaleTicketMapper {
    @Update("CREATE TABLE IF NOT EXISTS market.after_sale_ticket (" +
            "id VARCHAR(64) PRIMARY KEY COMMENT '工单id'," +
            "order_id VARCHAR(64) NOT NULL COMMENT '关联订单id'," +
            "customer_id VARCHAR(64) NOT NULL COMMENT '顾客id'," +
            "merchant_id VARCHAR(64) NOT NULL COMMENT '商家id'," +
            "type VARCHAR(32) NOT NULL COMMENT '工单类型'," +
            "content TEXT NOT NULL COMMENT '问题描述'," +
            "status VARCHAR(32) NOT NULL COMMENT '工单状态'," +
            "handler_id VARCHAR(64) COMMENT '处理人id'," +
            "handler_note TEXT COMMENT '处理备注'," +
            "created_time DATETIME NOT NULL COMMENT '创建时间'," +
            "updated_time DATETIME NOT NULL COMMENT '更新时间'," +
            "INDEX idx_after_sale_customer (customer_id)," +
            "INDEX idx_after_sale_merchant (merchant_id)," +
            "INDEX idx_after_sale_status (status)," +
            "INDEX idx_after_sale_order (order_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后工单表'")
    void createTableIfMissing();

    @Insert("INSERT INTO market.after_sale_ticket(id, order_id, customer_id, merchant_id, type, content, status, handler_id, handler_note, created_time, updated_time) " +
            "VALUES(#{id}, #{orderId}, #{customerId}, #{merchantId}, #{type}, #{content}, #{status}, #{handlerId}, #{handlerNote}, #{createdTime}, #{updatedTime})")
    void insert(AfterSaleTicket ticket);

    @Update("UPDATE market.after_sale_ticket SET status=#{status}, handler_id=#{handlerId}, handler_note=#{handlerNote}, updated_time=#{updatedTime} WHERE id=#{id}")
    int updateHandleInfo(AfterSaleTicket ticket);

    @Select("SELECT * FROM market.after_sale_ticket WHERE id=#{id}")
    AfterSaleTicket getById(String id);

    @Select("SELECT * FROM market.after_sale_ticket WHERE order_id=#{orderId} ORDER BY created_time DESC")
    List<AfterSaleTicket> listByOrderId(String orderId);

    @Select("<script>SELECT * FROM market.after_sale_ticket WHERE customer_id=#{customerId} " +
            "<if test='status != null and status != \"\"'>AND status=#{status} </if>" +
            "<if test='type != null and type != \"\"'>AND type=#{type} </if>" +
            "ORDER BY updated_time DESC</script>")
    List<AfterSaleTicket> listByCustomer(@Param("customerId") String customerId, @Param("status") String status, @Param("type") String type);

    @Select("<script>SELECT * FROM market.after_sale_ticket WHERE merchant_id=#{merchantId} " +
            "<if test='status != null and status != \"\"'>AND status=#{status} </if>" +
            "<if test='type != null and type != \"\"'>AND type=#{type} </if>" +
            "ORDER BY updated_time DESC</script>")
    List<AfterSaleTicket> listByMerchant(@Param("merchantId") String merchantId, @Param("status") String status, @Param("type") String type);

    @Select("<script>SELECT * FROM market.after_sale_ticket WHERE 1=1 " +
            "<if test='status != null and status != \"\"'>AND status=#{status} </if>" +
            "<if test='type != null and type != \"\"'>AND type=#{type} </if>" +
            "ORDER BY updated_time DESC</script>")
    List<AfterSaleTicket> listAll(@Param("status") String status, @Param("type") String type);
}
