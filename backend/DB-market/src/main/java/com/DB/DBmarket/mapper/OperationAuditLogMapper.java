package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.ops.OperationAuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OperationAuditLogMapper {
    @Update("CREATE TABLE IF NOT EXISTS market.operation_audit_log (" +
            "id VARCHAR(64) PRIMARY KEY COMMENT '日志id'," +
            "actor_id VARCHAR(64) COMMENT '操作人id'," +
            "actor_name VARCHAR(128) COMMENT '操作人名称'," +
            "actor_type VARCHAR(32) COMMENT '操作人角色'," +
            "action_type VARCHAR(64) NOT NULL COMMENT '动作类型'," +
            "target_type VARCHAR(64) COMMENT '目标类型'," +
            "target_id VARCHAR(64) COMMENT '目标id'," +
            "target_name VARCHAR(128) COMMENT '目标名称'," +
            "detail TEXT COMMENT '动作描述'," +
            "result VARCHAR(32) NOT NULL COMMENT '处理结果'," +
            "created_time DATETIME NOT NULL COMMENT '创建时间'," +
            "INDEX idx_operation_audit_actor (actor_id)," +
            "INDEX idx_operation_audit_type (action_type)," +
            "INDEX idx_operation_audit_time (created_time)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂展示版操作审计日志表'")
    void createTableIfMissing();

    @Insert("INSERT INTO market.operation_audit_log(id, actor_id, actor_name, actor_type, action_type, target_type, target_id, target_name, detail, result, created_time) " +
            "VALUES(#{id}, #{actorId}, #{actorName}, #{actorType}, #{actionType}, #{targetType}, #{targetId}, #{targetName}, #{detail}, #{result}, #{createdTime})")
    void insert(OperationAuditLog log);

    @Select("<script>SELECT * FROM market.operation_audit_log WHERE 1=1 " +
            "<if test='actorId != null and actorId != \"\"'>AND actor_id=#{actorId} </if>" +
            "<if test='actorType != null and actorType != \"\"'>AND actor_type=#{actorType} </if>" +
            "<if test='actionType != null and actionType != \"\"'>AND action_type=#{actionType} </if>" +
            "<if test='keyword != null and keyword != \"\"'>AND (" +
            "actor_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "target_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "detail LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY created_time DESC LIMIT 200</script>")
    List<OperationAuditLog> list(@Param("actorId") String actorId,
                                 @Param("actorType") String actorType,
                                 @Param("actionType") String actionType,
                                 @Param("keyword") String keyword);
}
