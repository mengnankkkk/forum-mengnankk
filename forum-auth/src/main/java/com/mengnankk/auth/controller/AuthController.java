package com.mengnankk.auth.controller;

import com.mengnankk.auth.annotation.RateLimit;
import com.mengnankk.auth.dto.*;
import com.mengnankk.auth.entity.User;
import com.mengnankk.auth.security.CustomUserDetails;
import com.mengnankk.auth.security.JwtTokenProvider;
import com.mengnankk.auth.service.UserService;
import com.mengnankk.auth.service.AuthEventProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthEventProducer authEventProducer;

    @Operation(summary = "用户登录", description = "通过用户名密码登录系统")
    @PostMapping("/login")
    @RateLimit(key = "login", time = 300, count = 5, message = "登录过于频繁，请5分钟后再试")
    public ResponseEntity<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 创建认证Token
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            
            // 进行认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 获取用户详情
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // 生成Token
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getTokenRemainingTime(accessToken));
            response.setUserInfo(buildUserInfo(userDetails));
            
            // 发送登录事件
            authEventProducer.sendLoginEvent(userDetails.getUserId(), userDetails.getUsername(), "password");
            
            return ResponseEntity.ok(Result.success(response));
            
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("登录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    @RateLimit(key = "register", time = 60, count = 3, message = "注册过于频繁，请1分钟后再试")
    public ResponseEntity<Result<UserInfo>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 创建用户对象
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setNickname(request.getNickname());
            
            // 注册用户
            User registeredUser = userService.register(user, request.getPassword());
            
            // 发送注册事件
            authEventProducer.sendRegisterEvent(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getEmail());
            
            return ResponseEntity.ok(Result.success(buildUserInfo(registeredUser)));
            
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("注册失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "刷新Token", description = "使用刷新Token获取新的访问Token")
    @PostMapping("/refresh")
    public ResponseEntity<Result<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = jwtTokenProvider.refreshToken(request.getRefreshToken());
            
            LoginResponse response = new LoginResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(request.getRefreshToken());
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getTokenRemainingTime(newAccessToken));
            
            return ResponseEntity.ok(Result.success(response));
            
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("Token刷新失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "用户登出", description = "登出当前用户，使Token失效")
    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                
                // 注销Token
                String token = getCurrentToken();
                if (token != null) {
                    jwtTokenProvider.revokeToken(token);
                }
                
                // 注销刷新Token
                jwtTokenProvider.revokeRefreshToken(userDetails.getUserId());
                
                // 发送登出事件
                authEventProducer.sendLogoutEvent(userDetails.getUserId(), userDetails.getUsername());
            }
            
            return ResponseEntity.ok(Result.success("登出成功"));
            
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("登出失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @PostMapping("/change-password")
    public ResponseEntity<Result<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return ResponseEntity.ok(Result.error("用户未认证"));
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // 修改密码
            userService.changePassword(userDetails.getUserId(), request.getOldPassword(), request.getNewPassword());
            
            // 发送密码修改事件
            authEventProducer.sendPasswordChangeEvent(userDetails.getUserId(), userDetails.getUsername());
            
            return ResponseEntity.ok(Result.success("密码修改成功"));
            
        } catch (Exception e) {
            log.error("密码修改失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("密码修改失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public ResponseEntity<Result<UserInfo>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return ResponseEntity.ok(Result.error("用户未认证"));
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return ResponseEntity.ok(Result.success(buildUserInfo(userDetails)));
            
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(Result.error("获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 构建用户信息DTO
     */
    private UserInfo buildUserInfo(CustomUserDetails userDetails) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userDetails.getUserId());
        userInfo.setUsername(userDetails.getUsername());
        userInfo.setEmail(userDetails.getEmail());
        userInfo.setPhone(userDetails.getPhone());
        userInfo.setNickname(userDetails.getNickname());
        userInfo.setAvatar(userDetails.getAvatar());
        userInfo.setEmailVerified(userDetails.isEmailVerified() ? 1 : 0);
        userInfo.setPhoneVerified(userDetails.isPhoneVerified() ? 1 : 0);
        userInfo.setRoles(userDetails.getRoles());
        userInfo.setPermissions(userDetails.getPermissions());
        return userInfo;
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
        userInfo.setEmailVerified(user.isEmailVerified() ? 1 : 0);
        userInfo.setPhoneVerified(user.isPhoneVerified() ? 1 : 0);
        userInfo.setRoles(userService.getUserRoles(user.getId()));
        userInfo.setPermissions(userService.getUserPermissions(user.getId()));
        return userInfo;
    }

    /**
     * 获取当前请求的Token
     */
    private String getCurrentToken() {
        // 这里可以从请求头中获取Token
        // 具体实现可以根据需要调整
        return null;
    }
}
