package com.mengnankk.auth.feign;

import com.mengnankk.auth.dto.Result;
import com.mengnankk.auth.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证服务Feign客户端接口
 * 供其他模块调用认证服务
 */
@FeignClient(name = "forum-auth", path = "/api/auth")
public interface AuthFeignClient {

    /**
     * 验证Token并获取用户信息
     */
    @GetMapping("/validate")
    Result<UserInfo> validateToken(@RequestHeader("Authorization") String token);

    /**
     * 获取用户信息
     */
    @GetMapping("/user/{userId}")
    Result<UserInfo> getUserInfo(@PathVariable("userId") Long userId);

    /**
     * 检查用户权限
     */
    @GetMapping("/permission/check")
    Result<Boolean> hasPermission(
            @RequestParam("userId") Long userId,
            @RequestParam("permission") String permission);

    /**
     * 检查用户角色
     */
    @GetMapping("/role/check")
    Result<Boolean> hasRole(
            @RequestParam("userId") Long userId,
            @RequestParam("role") String role);

    /**
     * 获取用户权限列表
     */
    @GetMapping("/user/{userId}/permissions")
    Result<List<String>> getUserPermissions(@PathVariable("userId") Long userId);

    /**
     * 获取用户角色列表
     */
    @GetMapping("/user/{userId}/roles")
    Result<List<String>> getUserRoles(@PathVariable("userId") Long userId);
}
