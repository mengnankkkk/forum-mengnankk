package com.mengnankk.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengnankk.auth.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询角色的权限列表
     */
    @Select("SELECT p.permission_name FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<String> findRolePermissions(@Param("roleId") Long roleId);

    /**
     * 为用户分配角色
     */
    @Insert("INSERT INTO user_roles (user_id, role_id, created_time) " +
            "VALUES (#{userId}, #{roleId}, NOW())")
    int assignRoleToUser(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 查询默认角色
     */
    @Select("SELECT * FROM roles WHERE is_default = 1 LIMIT 1")
    Role findDefaultRole();

    /**
     * 查询用户的角色列表
     */
    @Select("SELECT r.role_name FROM roles r " +
            "INNER JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> findUserRoles(@Param("userId") Long userId);

    /**
     * 查询用户的权限列表
     */
    @Select("SELECT DISTINCT p.permission_name FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> findUserPermissions(@Param("userId") Long userId);

    /**
     * 移除用户的所有角色
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int removeAllRolesFromUser(@Param("userId") Long userId);

    /**
     * 移除用户的指定角色
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
