package com.mengnankk.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengnankk.auth.entity.Permission;
import com.mengnankk.auth.mapper.PermissionMapper;
import com.mengnankk.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService extends ServiceImpl<PermissionMapper, Permission> {

    /**
     * 根据权限名查询权限
     */
    public Permission findByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("permission_name", name);
        return this.getOne(queryWrapper);
    }

    /**
     * 创建权限
     */
    @Transactional(rollbackFor = Exception.class)
    public Permission createPermission(Permission permission) {
        // 检查权限名是否已存在
        if (findByName(permission.getPermissionName()) != null) {
            throw new AuthException("权限名已存在: " + permission.getPermissionName());
        }
        
        permission.setStatus(Permission.STATUS_ENABLED);
        permission.setCreatedTime(LocalDateTime.now());
        permission.setUpdatedTime(LocalDateTime.now());
        
        this.save(permission);
        log.info("权限创建成功: {}", permission.getPermissionName());
        return permission;
    }

    /**
     * 更新权限
     */
    @Transactional(rollbackFor = Exception.class)
    public Permission updatePermission(Permission permission) {
        Permission existingPermission = this.getById(permission.getId());
        if (existingPermission == null) {
            throw new AuthException("权限不存在");
        }
        
        // 检查权限名是否被其他权限使用
        Permission permissionWithSameName = findByName(permission.getPermissionName());
        if (permissionWithSameName != null && !permissionWithSameName.getId().equals(permission.getId())) {
            throw new AuthException("权限名已存在: " + permission.getPermissionName());
        }
        
        permission.setUpdatedTime(LocalDateTime.now());
        this.updateById(permission);
        
        log.info("权限更新成功: {}", permission.getPermissionName());
        return permission;
    }

    /**
     * 删除权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permissionId) {
        Permission permission = this.getById(permissionId);
        if (permission == null) {
            throw new AuthException("权限不存在");
        }
        
        // 删除权限相关的角色关联
        baseMapper.removeAllPermissionsFromRole(permissionId);
        
        // 删除权限
        this.removeById(permissionId);
        
        log.info("权限删除成功: {}", permission.getPermissionName());
    }

    /**
     * 启用/禁用权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void togglePermissionStatus(Long permissionId) {
        Permission permission = this.getById(permissionId);
        if (permission == null) {
            throw new AuthException("权限不存在");
        }
        
        int newStatus = permission.getStatus().equals(Permission.STATUS_ENABLED) ? 
                        Permission.STATUS_DISABLED : Permission.STATUS_ENABLED;
        
        permission.setStatus(newStatus);
        permission.setUpdatedTime(LocalDateTime.now());
        this.updateById(permission);
        
        log.info("权限状态切换成功: {} -> {}", permission.getPermissionName(), 
                newStatus == Permission.STATUS_ENABLED ? "启用" : "禁用");
    }

    /**
     * 分页查询权限列表
     */
    public IPage<Permission> pagePermissions(int pageNum, int pageSize, String keyword) {
        Page<Permission> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("permission_name", keyword)
                    .or()
                    .like("description", keyword));
        }
        
        queryWrapper.orderByDesc("created_time");
        return this.page(page, queryWrapper);
    }

    /**
     * 为角色分配权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        baseMapper.assignPermissionToRole(roleId, permissionId);
        log.info("为角色分配权限成功: roleId={}, permissionId={}", roleId, permissionId);
    }

    /**
     * 移除角色的权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        baseMapper.removePermissionFromRole(roleId, permissionId);
        log.info("移除角色权限成功: roleId={}, permissionId={}", roleId, permissionId);
    }

    /**
     * 移除角色的所有权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllPermissionsFromRole(Long roleId) {
        baseMapper.removeAllPermissionsFromRole(roleId);
        log.info("移除角色所有权限成功: roleId={}", roleId);
    }

    /**
     * 根据权限类型查询权限列表
     */
    public List<Permission> findByType(String type) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("permission_type", type);
        queryWrapper.eq("status", Permission.STATUS_ENABLED);
        queryWrapper.orderByAsc("sort_order");
        return this.list(queryWrapper);
    }

    /**
     * 获取所有启用的权限
     */
    public List<Permission> findAllEnabled() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Permission.STATUS_ENABLED);
        queryWrapper.orderByAsc("sort_order");
        return this.list(queryWrapper);
    }
}
