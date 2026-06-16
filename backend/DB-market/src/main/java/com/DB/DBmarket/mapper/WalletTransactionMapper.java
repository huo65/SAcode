package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.wallet.WalletTransaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WalletTransactionMapper {
    @Update("CREATE TABLE IF NOT EXISTS market.wallet_transaction (" +
            "id VARCHAR(64) PRIMARY KEY COMMENT '流水id'," +
            "user_id VARCHAR(64) NOT NULL COMMENT '钱包用户id'," +
            "user_name VARCHAR(128) COMMENT '钱包用户名称'," +
            "type VARCHAR(32) NOT NULL COMMENT '流水类型 RECHARGE/PAY/REFUND/ADJUST'," +
            "amount INT NOT NULL COMMENT '变动金额，收入为正，支出为负'," +
            "balance_before INT NOT NULL COMMENT '变动前余额'," +
            "balance_after INT NOT NULL COMMENT '变动后余额'," +
            "related_order_id VARCHAR(64) COMMENT '关联订单id'," +
            "remark VARCHAR(500) COMMENT '备注'," +
            "actor_id VARCHAR(64) COMMENT '操作人id'," +
            "actor_name VARCHAR(128) COMMENT '操作人名称'," +
            "actor_type VARCHAR(32) COMMENT '操作人角色'," +
            "created_time DATETIME NOT NULL COMMENT '创建时间'," +
            "INDEX idx_wallet_transaction_user (user_id)," +
            "INDEX idx_wallet_transaction_type (type)," +
            "INDEX idx_wallet_transaction_order (related_order_id)," +
            "INDEX idx_wallet_transaction_time (created_time)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包余额流水表'")
    void createTableIfMissing();

    @Insert("INSERT INTO market.wallet_transaction(id, user_id, user_name, type, amount, balance_before, balance_after, related_order_id, remark, actor_id, actor_name, actor_type, created_time) " +
            "VALUES(#{id}, #{userId}, #{userName}, #{type}, #{amount}, #{balanceBefore}, #{balanceAfter}, #{relatedOrderId}, #{remark}, #{actorId}, #{actorName}, #{actorType}, #{createdTime})")
    void insert(WalletTransaction transaction);

    @Select("<script>SELECT * FROM market.wallet_transaction WHERE 1=1 " +
            "<if test='userId != null and userId != \"\"'>AND user_id=#{userId} </if>" +
            "<if test='type != null and type != \"\"'>AND type=#{type} </if>" +
            "ORDER BY created_time DESC LIMIT #{limit}</script>")
    List<WalletTransaction> list(@Param("userId") String userId,
                                 @Param("type") String type,
                                 @Param("limit") Integer limit);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM market.wallet_transaction WHERE user_id=#{userId} AND type=#{type}")
    Integer sumByUserAndType(@Param("userId") String userId, @Param("type") String type);
}
