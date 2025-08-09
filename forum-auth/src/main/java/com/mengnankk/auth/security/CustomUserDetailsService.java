package com.mengnankk.auth.security;

import com.mengnankk.auth.entity.User;
import com.mengnankk.auth.service.UserService;
import com.mengnankk.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义用户详情服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (!user.isEnabled()) {
            log.warn("用户已被禁用: {}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 获取用户的角色和权限
        List<String> roles = roleService.getUserRoles(user.getId());
        List<String> permissions = roleService.getUserPermissions(user.getId());

        log.debug("用户登录: {}, 角色: {}, 权限: {}", username, roles, permissions);
        
        return new CustomUserDetails(user, roles, permissions);
    }

    /**
     * 根据用户ID加载用户详情
     */
    public UserDetails loadUserByUserId(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        if (!user.isEnabled()) {
            log.warn("用户已被禁用: userId={}", userId);
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }

        // 获取用户的角色和权限
        List<String> roles = roleService.getUserRoles(user.getId());
        List<String> permissions = roleService.getUserPermissions(user.getId());

        return new CustomUserDetails(user, roles, permissions);
    }
}
