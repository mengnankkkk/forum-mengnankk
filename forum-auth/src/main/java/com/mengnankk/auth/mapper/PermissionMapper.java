package com.mengnankk.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengnankk.auth.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

/**
 * 权限数据访问层
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 为角色分配权限
     */
    @Insert("INSERT INTO role_permissions (role_id, permission_id, created_time) " +
            "VALUES (#{roleId}, #{permissionId}, NOW())")
    int assignPermissionToRole(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 移除角色的权限
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int removePermissionFromRole(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 移除角色的所有权限
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int removeAllPermissionsFromRole(@Param("roleId") Long roleId);
}
