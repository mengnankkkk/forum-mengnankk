package com.mengnankk.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengnankk.auth.entity.Role;
import com.mengnankk.auth.mapper.RoleMapper;
import com.mengnankk.auth.constants.AuthConstants;
import com.mengnankk.auth.exception.AuthException;
import com.mengnankk.auth.util.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PermissionService permissionService;

    /**
     * 根据角色名查询角色
     */
    public Role findByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_name", name);
        return this.getOne(queryWrapper);
    }

    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    public Role createRole(Role role) {
        // 检查角色名是否已存在
        if (findByName(role.getRoleName()) != null) {
            throw new AuthException("角色名已存在: " + role.getRoleName());
        }
        
        role.setStatus(Role.STATUS_ENABLED);
        role.setCreatedTime(LocalDateTime.now());
        role.setUpdatedTime(LocalDateTime.now());
        
        this.save(role);
        log.info("角色创建成功: {}", role.getRoleName());
        return role;
    }

    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public Role updateRole(Role role) {
        Role existingRole = this.getById(role.getId());
        if (existingRole == null) {
            throw new AuthException("角色不存在");
        }
        
        // 检查角色名是否被其他角色使用
        Role roleWithSameName = findByName(role.getRoleName());
        if (roleWithSameName != null && !roleWithSameName.getId().equals(role.getId())) {
            throw new AuthException("角色名已存在: " + role.getRoleName());
        }
        
        role.setUpdatedTime(LocalDateTime.now());
        this.updateById(role);
        
        // 清除相关缓存
        clearRoleRelatedCache(role.getId());
        
        log.info("角色更新成功: {}", role.getRoleName());
        return role;
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        Role role = this.getById(roleId);
        if (role == null) {
            throw new AuthException("角色不存在");
        }
        
        // 检查是否为系统内置角色
        if (AuthConstants.DEFAULT_ROLE.equals(role.getRoleName()) ||
            AuthConstants.ADMIN_ROLE.equals(role.getRoleName()) ||
            AuthConstants.SUPER_ADMIN_ROLE.equals(role.getRoleName())) {
            throw new AuthException("系统内置角色不能删除");
        }
        
        // 删除角色相关的权限关联
        permissionService.removeAllPermissionsFromRole(roleId);
        
        // 删除角色
        this.removeById(roleId);
        
        // 清除相关缓存
        clearRoleRelatedCache(roleId);
        
        log.info("角色删除成功: {}", role.getRoleName());
    }

    /**
     * 启用/禁用角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleRoleStatus(Long roleId) {
        Role role = this.getById(roleId);
        if (role == null) {
            throw new AuthException("角色不存在");
        }
        
        int newStatus = role.getStatus().equals(Role.STATUS_ENABLED) ? 
                        Role.STATUS_DISABLED : Role.STATUS_ENABLED;
        
        role.setStatus(newStatus);
        role.setUpdatedTime(LocalDateTime.now());
        this.updateById(role);
        
        // 清除相关缓存
        clearRoleRelatedCache(roleId);
        
        log.info("角色状态切换成功: {} -> {}", role.getRoleName(), 
                newStatus == Role.STATUS_ENABLED ? "启用" : "禁用");
    }

    /**
     * 分页查询角色列表
     */
    public IPage<Role> pageRoles(int pageNum, int pageSize, String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("role_name", keyword)
                    .or()
                    .like("description", keyword));
        }
        
        queryWrapper.orderByDesc("created_time");
        return this.page(page, queryWrapper);
    }

    /**
     * 获取用户角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(Long userId) {
        String cacheKey = RedisKeys.USER_ROLES + userId;
        List<String> cachedRoles = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRoles != null) {
            return cachedRoles;
        }
        
        List<String> roles = baseMapper.findUserRoles(userId);
        redisTemplate.opsForValue().set(cacheKey, roles, 30, TimeUnit.MINUTES);
        return roles;
    }

    /**
     * 获取用户权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(Long userId) {
        String cacheKey = RedisKeys.USER_PERMISSIONS + userId;
        List<String> cachedPermissions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }
        
        List<String> permissions = baseMapper.findUserPermissions(userId);
        redisTemplate.opsForValue().set(cacheKey, permissions, 30, TimeUnit.MINUTES);
        return permissions;
    }

    /**
     * 为用户分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleToUser(Long userId, Long roleId) {
        baseMapper.assignRoleToUser(userId, roleId);
        
        // 清除用户相关缓存
        clearUserRelatedCache(userId);
        
        log.info("为用户分配角色成功: userId={}, roleId={}", userId, roleId);
    }

    /**
     * 为用户分配默认角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignDefaultRole(Long userId) {
        Role defaultRole = baseMapper.findDefaultRole();
        if (defaultRole == null) {
            // 如果没有默认角色，查找普通用户角色
            defaultRole = findByName(AuthConstants.DEFAULT_ROLE);
            if (defaultRole == null) {
                log.warn("未找到默认角色，跳过角色分配: userId={}", userId);
                return;
            }
        }
        
        assignRoleToUser(userId, defaultRole.getId());
    }

    /**
     * 获取角色的权限列表
     */
    public List<String> getRolePermissions(Long roleId) {
        return baseMapper.findRolePermissions(roleId);
    }

    /**
     * 清除角色相关缓存
     */
    private void clearRoleRelatedCache(Long roleId) {
        // 这里可以实现更复杂的缓存清理逻辑
        // 比如清理所有拥有该角色的用户的权限缓存
    }

    /**
     * 清除用户相关缓存
     */
    private void clearUserRelatedCache(Long userId) {
        String rolesCacheKey = RedisKeys.USER_ROLES + userId;
        String permissionsCacheKey = RedisKeys.USER_PERMISSIONS + userId;
        
        redisTemplate.delete(rolesCacheKey);
        redisTemplate.delete(permissionsCacheKey);
    }

    /**
     * 批量分配角色给用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 先清除用户现有角色
        baseMapper.removeAllRolesFromUser(userId);
        
        // 分配新角色
        for (Long roleId : roleIds) {
            assignRoleToUser(userId, roleId);
        }
        
        log.info("批量分配角色成功: userId={}, roleIds={}", userId, roleIds);
    }

    /**
     * 批量移除用户角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            baseMapper.removeRoleFromUser(userId, roleId);
        }
        
        // 清除用户相关缓存
        clearUserRelatedCache(userId);
        
        log.info("批量移除用户角色成功: userId={}, roleIds={}", userId, roleIds);
    }
}
