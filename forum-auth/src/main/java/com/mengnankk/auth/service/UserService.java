package com.mengnankk.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengnankk.auth.entity.User;
import com.mengnankk.auth.mapper.UserMapper;
import com.mengnankk.auth.util.EncryptUtils;
import com.mengnankk.auth.util.RedisKeys;
import com.mengnankk.auth.exception.AuthException;
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
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleService roleService;

    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        
        // 先从缓存查询
        String cacheKey = RedisKeys.USER_INFO + username;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // 从数据库查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = this.getOne(queryWrapper);
        
        // 缓存用户信息
        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user, 30, TimeUnit.MINUTES);
        }
        
        return user;
    }

    /**
     * 根据邮箱查询用户
     */
    public User findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据手机号查询用户
     */
    public User findByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return this.getOne(queryWrapper);
    }

    /**
     * 注册用户
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(User user, String plainPassword) {
        // 检查用户名是否已存在
        if (findByUsername(user.getUsername()) != null) {
            throw new AuthException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) && findByEmail(user.getEmail()) != null) {
            throw new AuthException("邮箱已被注册");
        }
        
        // 检查手机号是否已存在
        if (StringUtils.hasText(user.getPhone()) && findByPhone(user.getPhone()) != null) {
            throw new AuthException("手机号已被注册");
        }
        
        // 密码加密
        user.setPassword(EncryptUtils.encryptPassword(plainPassword));
        
        // 设置默认值
        user.setStatus(User.STATUS_ENABLED);
        user.setEmailVerified(User.EMAIL_NOT_VERIFIED);
        user.setPhoneVerified(User.PHONE_NOT_VERIFIED);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        
        // 保存用户
        this.save(user);
        
        // 分配默认角色
        roleService.assignDefaultRole(user.getId());
        
        log.info("用户注册成功: {}", user.getUsername());
        return user;
    }

    /**
     * 为用户分配默认角色
     */
    public void assignDefaultRole(Long userId) {
        roleService.assignDefaultRole(userId);
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        User existingUser = this.getById(user.getId());
        if (existingUser == null) {
            throw new AuthException("用户不存在");
        }
        
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + existingUser.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户信息更新成功: {}", user.getUsername());
        return user;
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        // 验证旧密码
        if (!EncryptUtils.verifyPassword(oldPassword, user.getPassword())) {
            throw new AuthException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(EncryptUtils.encryptPassword(newPassword));
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户密码修改成功: {}", user.getUsername());
    }

    /**
     * 启用/禁用用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        int newStatus = user.getStatus().equals(User.STATUS_ENABLED) ? 
                        User.STATUS_DISABLED : User.STATUS_ENABLED;
        
        user.setStatus(newStatus);
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户状态切换成功: {} -> {}", user.getUsername(), 
                newStatus == User.STATUS_ENABLED ? "启用" : "禁用");
    }

    /**
     * 分页查询用户列表
     */
    public IPage<User> pageUsers(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("nickname", keyword)
                    .or()
                    .like("email", keyword));
        }
        
        queryWrapper.orderByDesc("created_time");
        return this.page(page, queryWrapper);
    }

    /**
     * 验证邮箱
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        user.setEmailVerified(User.EMAIL_VERIFIED);
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户邮箱验证成功: {}", user.getUsername());
    }

    /**
     * 验证手机号
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyPhone(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        user.setPhoneVerified(User.PHONE_VERIFIED);
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户手机号验证成功: {}", user.getUsername());
    }

    /**
     * 获取用户的角色列表
     */
    public List<String> getUserRoles(Long userId) {
        return roleService.getUserRoles(userId);
    }

    /**
     * 获取用户的权限列表
     */
    public List<String> getUserPermissions(Long userId) {
        return roleService.getUserPermissions(userId);
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询用户列表 (兼容Spring Data)
     */
    public org.springframework.data.domain.Page<User> getUserList(String keyword, org.springframework.data.domain.Pageable pageable) {
        IPage<User> mybatisPlusPage = pageUsers(
            (int) pageable.getPageNumber() + 1, 
            pageable.getPageSize(), 
            keyword
        );
        
        return new org.springframework.data.domain.PageImpl<>(
            mybatisPlusPage.getRecords(),
            pageable,
            mybatisPlusPage.getTotal()
        );
    }

    /**
     * 软删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        user.setDeleted(1); // 1表示已删除
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户删除成功: {}", user.getUsername());
    }

    /**
     * 更新用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        user.setStatus(status);
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户状态更新成功: {} -> {}", user.getUsername(), status);
    }

    /**
     * 重置用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        // 生成新密码
        String newPassword = generateRandomPassword();
        user.setPassword(EncryptUtils.encryptPassword(newPassword));
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);
        
        // 清除缓存
        String cacheKey = RedisKeys.USER_INFO + user.getUsername();
        redisTemplate.delete(cacheKey);
        
        log.info("用户密码重置成功: {}", user.getUsername());
        return newPassword;
    }

    /**
     * 分配角色给用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        roleService.assignRolesToUser(userId, roleIds);
    }

    /**
     * 移除用户角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRoles(Long userId, List<Long> roleIds) {
        roleService.removeRolesFromUser(userId, roleIds);
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}
