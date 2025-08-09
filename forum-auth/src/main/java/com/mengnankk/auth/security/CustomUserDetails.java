package com.mengnankk.auth.security;

import com.mengnankk.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String email;
    private final String phone;
    private final String nickname;
    private final String avatar;
    private final boolean enabled;
    private final boolean emailVerified;
    private final boolean phoneVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, List<String> roles, List<String> permissions) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.enabled = user.isEnabled();
        this.emailVerified = user.isEmailVerified();
        this.phoneVerified = user.isPhoneVerified();
        
        // 合并角色和权限为权限集合
        this.authorities = buildAuthorities(roles, permissions);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(List<String> roles, List<String> permissions) {
        // 角色添加ROLE_前缀
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        // 权限直接添加
        List<GrantedAuthority> permissionAuthorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        authorities.addAll(permissionAuthorities);
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    /**
     * 获取所有角色（不含ROLE_前缀）
     */
    public List<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 移除ROLE_前缀
                .collect(Collectors.toList());
    }

    /**
     * 获取所有权限（不含角色）
     */
    public List<String> getPermissions() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());
    }
}
