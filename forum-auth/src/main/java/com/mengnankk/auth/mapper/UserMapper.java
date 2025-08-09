package com.mengnankk.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengnankk.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户详细信息（包含角色和权限）
     */
    @Select("SELECT u.*, r.role_name as role_name, p.permission_name as permission_name " +
            "FROM users u " +
            "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
            "LEFT JOIN roles r ON ur.role_id = r.id " +
            "LEFT JOIN role_permissions rp ON r.id = rp.role_id " +
            "LEFT JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE u.username = #{username}")
    User findUserWithRolesAndPermissions(@Param("username") String username);

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
}
