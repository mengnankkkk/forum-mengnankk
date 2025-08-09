package com.mengnankk.auth.controller;

import com.mengnankk.auth.annotation.RateLimit;
import com.mengnankk.auth.dto.*;
import com.mengnankk.auth.entity.User;
import com.mengnankk.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户列表", description = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Result<PageResponse<UserInfo>>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<User> userPage = userService.getUserList(keyword, pageable);
            
            List<UserInfo> userInfoList = userPage.getContent().stream()
                    .map(this::buildUserInfo)
                    .toList();
            
            PageResponse<UserInfo> pageResponse = new PageResponse<>();
            pageResponse.setRecords(userInfoList);
            pageResponse.setCurrent((long) page);
            pageResponse.setSize((long) size);
            pageResponse.setTotal(userPage.getTotalElements());
            pageResponse.setPages((long) userPage.getTotalPages());
            pageResponse.setHasPrevious(userPage.hasPrevious());
            pageResponse.setHasNext(userPage.hasNext());
            
            return ResponseEntity.ok(Result.success(pageResponse));
            
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Result<UserInfo>> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }
            
            return ResponseEntity.ok(Result.success(buildUserInfo(user)));
            
        } catch (Exception e) {
            log.error("获取用户详情失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("获取用户详情失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @RateLimit(key = "create_user", time = 60, count = 10, message = "创建用户过于频繁")
    public ResponseEntity<Result<UserInfo>> createUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setNickname(request.getNickname());
            
            User createdUser = userService.register(user, request.getPassword());
            
            return ResponseEntity.ok(Result.success(buildUserInfo(createdUser)));
            
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("创建用户失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Result<UserInfo>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserInfo userInfo) {
        try {
            User user = new User();
            user.setId(userId);
            user.setNickname(userInfo.getNickname());
            user.setAvatar(userInfo.getAvatar());
            user.setPhone(userInfo.getPhone());
            user.setStatus(userInfo.getStatus());
            
            User updatedUser = userService.updateUser(user);
            
            return ResponseEntity.ok(Result.success(buildUserInfo(updatedUser)));
            
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("更新用户信息失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "删除用户", description = "软删除用户")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Result<String>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Result.success("用户删除成功"));
            
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("删除用户失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "启用/禁用用户", description = "启用或禁用用户账号")
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Result<String>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        try {
            userService.updateUserStatus(userId, status);
            String message = status == 1 ? "用户已启用" : "用户已禁用";
            return ResponseEntity.ok(Result.success(message));
            
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("更新用户状态失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Result<String>> resetPassword(@PathVariable Long userId) {
        try {
            String newPassword = userService.resetPassword(userId);
            return ResponseEntity.ok(Result.success("密码重置成功，新密码为: " + newPassword));
            
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("重置密码失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "分配用户角色", description = "为用户分配角色")
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user:role:assign')")
    public ResponseEntity<Result<String>> assignRoles(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        try {
            userService.assignRoles(userId, roleIds);
            return ResponseEntity.ok(Result.success("角色分配成功"));
            
        } catch (Exception e) {
            log.error("分配角色失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("分配角色失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "移除用户角色", description = "移除用户的角色")
    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user:role:assign')")
    public ResponseEntity<Result<String>> removeRoles(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        try {
            userService.removeRoles(userId, roleIds);
            return ResponseEntity.ok(Result.success("角色移除成功"));
            
        } catch (Exception e) {
            log.error("移除角色失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("移除角色失败: " + e.getMessage()));
        }
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
