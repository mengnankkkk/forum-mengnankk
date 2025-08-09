package com.mengnankk.auth.controller;

import com.mengnankk.auth.dto.Result;
import com.mengnankk.auth.dto.UserInfo;
import com.mengnankk.auth.entity.User;
import com.mengnankk.auth.security.JwtTokenProvider;
import com.mengnankk.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内部API控制器
 * 供其他微服务模块调用
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
@Tag(name = "内部认证API", description = "供其他微服务模块调用的认证接口")
public class InternalAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Operation(summary = "验证Token", description = "验证JWT Token并返回用户信息")
    @GetMapping("/validate")
    public ResponseEntity<Result<UserInfo>> validateToken(
            @RequestHeader("Authorization") String authorization) {
        try {
            // 提取Token
            String token = extractToken(authorization);
            if (token == null) {
                return ResponseEntity.ok(Result.error("Token格式错误"));
            }

            // 验证Token
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.ok(Result.error("Token无效或已过期"));
            }

            // 获取用户ID
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Result.error("无法从Token中获取用户信息"));
            }

            // 获取用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            // 构建用户信息
            UserInfo userInfo = buildUserInfo(user);
            return ResponseEntity.ok(Result.success(userInfo));

        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("Token验证失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Result<UserInfo>> getUserInfo(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            UserInfo userInfo = buildUserInfo(user);
            return ResponseEntity.ok(Result.success(userInfo));

        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("获取用户信息失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "检查用户权限", description = "检查用户是否具有指定权限")
    @GetMapping("/permission/check")
    public ResponseEntity<Result<Boolean>> hasPermission(
            @RequestParam Long userId,
            @RequestParam String permission) {
        try {
            List<String> userPermissions = userService.getUserPermissions(userId);
            boolean hasPermission = userPermissions.contains(permission);
            return ResponseEntity.ok(Result.success(hasPermission));

        } catch (Exception e) {
            log.error("权限检查失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("权限检查失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "检查用户角色", description = "检查用户是否具有指定角色")
    @GetMapping("/role/check")
    public ResponseEntity<Result<Boolean>> hasRole(
            @RequestParam Long userId,
            @RequestParam String role) {
        try {
            List<String> userRoles = userService.getUserRoles(userId);
            boolean hasRole = userRoles.contains(role);
            return ResponseEntity.ok(Result.success(hasRole));

        } catch (Exception e) {
            log.error("角色检查失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("角色检查失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取用户权限列表", description = "获取用户的所有权限")
    @GetMapping("/user/{userId}/permissions")
    public ResponseEntity<Result<List<String>>> getUserPermissions(@PathVariable Long userId) {
        try {
            List<String> permissions = userService.getUserPermissions(userId);
            return ResponseEntity.ok(Result.success(permissions));

        } catch (Exception e) {
            log.error("获取用户权限失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("获取用户权限失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取用户角色列表", description = "获取用户的所有角色")
    @GetMapping("/user/{userId}/roles")
    public ResponseEntity<Result<List<String>>> getUserRoles(@PathVariable Long userId) {
        try {
            List<String> roles = userService.getUserRoles(userId);
            return ResponseEntity.ok(Result.success(roles));

        } catch (Exception e) {
            log.error("获取用户角色失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("获取用户角色失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "批量获取用户信息", description = "根据用户ID列表批量获取用户信息")
    @PostMapping("/users/batch")
    public ResponseEntity<Result<List<UserInfo>>> getUsersBatch(@RequestBody List<Long> userIds) {
        try {
            List<UserInfo> userInfoList = userIds.stream()
                    .map(userId -> {
                        User user = userService.getUserById(userId);
                        return user != null ? buildUserInfo(user) : null;
                    })
                    .filter(userInfo -> userInfo != null)
                    .toList();

            return ResponseEntity.ok(Result.success(userInfoList));

        } catch (Exception e) {
            log.error("批量获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("批量获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 从Authorization头中提取Token
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 构建用户信息DTO
     */
    private UserInfo buildUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setStatus(user.getStatus());
        userInfo.setEmailVerified(user.isEmailVerified() ? 1 : 0);
        userInfo.setPhoneVerified(user.isPhoneVerified() ? 1 : 0);
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setCreatedTime(user.getCreatedTime());
        userInfo.setRoles(userService.getUserRoles(user.getId()));
        userInfo.setPermissions(userService.getUserPermissions(user.getId()));
        return userInfo;
    }
}
