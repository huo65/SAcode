package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.ops.OperationPermission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OperationPermissionMapper {
    @Update("CREATE TABLE IF NOT EXISTS market.operation_permission (" +
            "role_code VARCHAR(32) NOT NULL COMMENT '角色编码'," +
            "permission_key VARCHAR(128) NOT NULL COMMENT '权限键'," +
            "permission_name VARCHAR(128) NOT NULL COMMENT '权限名称'," +
            "permission_type VARCHAR(32) NOT NULL COMMENT '权限类型 menu/action'," +
            "scope_code VARCHAR(32) NOT NULL COMMENT 'admin/merchant'," +
            "enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用'," +
            "updated_time DATETIME NOT NULL COMMENT '更新时间'," +
            "PRIMARY KEY (role_code, permission_key)," +
            "INDEX idx_operation_permission_scope (scope_code, permission_type)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂展示版权限配置表'")
    void createTableIfMissing();

    @Insert("INSERT INTO market.operation_permission(role_code, permission_key, permission_name, permission_type, scope_code, enabled, updated_time) " +
            "VALUES(#{roleCode}, #{permissionKey}, #{permissionName}, #{permissionType}, #{scopeCode}, #{enabled}, #{updatedTime}) " +
            "ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name), permission_type=VALUES(permission_type), " +
            "scope_code=VALUES(scope_code)")
    void upsert(OperationPermission permission);

    @Update("UPDATE market.operation_permission SET enabled=#{enabled}, updated_time=#{updatedTime} " +
            "WHERE role_code=#{roleCode} AND permission_key=#{permissionKey}")
    int updateEnabled(@Param("roleCode") String roleCode,
                      @Param("permissionKey") String permissionKey,
                      @Param("enabled") Integer enabled,
                      @Param("updatedTime") java.time.LocalDateTime updatedTime);

    @Select("SELECT * FROM market.operation_permission ORDER BY scope_code, role_code, permission_type, permission_key")
    List<OperationPermission> listAll();

    @Select("SELECT * FROM market.operation_permission WHERE role_code=#{roleCode} ORDER BY permission_type, permission_key")
    List<OperationPermission> listByRole(@Param("roleCode") String roleCode);

    @Select("SELECT * FROM market.operation_permission WHERE role_code=#{roleCode} AND permission_key=#{permissionKey}")
    OperationPermission getByRoleAndKey(@Param("roleCode") String roleCode, @Param("permissionKey") String permissionKey);
}
