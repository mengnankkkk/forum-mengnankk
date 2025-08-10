package com.mengnankk.auth.security;

// 临时注释掉未使用的导入
/*
import com.mengnankk.auth.entity.User;
import com.mengnankk.auth.entity.OAuth2Account;
import com.mengnankk.auth.service.UserService;
import com.mengnankk.auth.service.OAuth2AccountService;
import com.mengnankk.auth.service.AuthEventProducer;
import com.mengnankk.auth.constants.AuthConstants;
import com.mengnankk.auth.util.EncryptUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
*/

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth2登录成功处理器
 * 临时注释掉所有功能，等OAuth2依赖问题解决后再启用
 */
@Slf4j
@Component
// @RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 临时注释掉所有字段
    /*
    private final UserService userService;
    private final OAuth2AccountService oAuth2AccountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthEventProducer authEventProducer;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;
    */

    // 临时注释掉OAuth2处理，直到依赖问题解决
    /*
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        try {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            
            log.info("OAuth2登录成功: provider={}, user={}", registrationId, oauth2User.getName());
            
            // 处理OAuth2用户信息
            User user = processOAuth2User(oauth2User, registrationId);
            
            // 生成JWT Token
            CustomUserDetails userDetails = new CustomUserDetails(
                user, 
                userService.getUserRoles(user.getId()),
                userService.getUserPermissions(user.getId())
            );
            
            // 创建Authentication对象用于Token生成
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken jwtAuth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                    
            String accessToken = jwtTokenProvider.generateToken(jwtAuth);
            String refreshToken = jwtTokenProvider.generateRefreshToken(jwtAuth);
            
            // 发送登录事件
            authEventProducer.sendLoginEvent(user.getId(), user.getUsername(), "oauth2_" + registrationId);
            
            // 重定向到前端，携带Token
            String targetUrl = buildTargetUrl(accessToken, refreshToken);
            
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("OAuth2登录处理失败: {}", e.getMessage(), e);
            
            String errorUrl = redirectUri + "?error=" + 
                URLEncoder.encode("登录失败: " + e.getMessage(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
    */

    /**
     * 处理OAuth2用户信息
     */
    /*
    private User processOAuth2User(OAuth2User oauth2User, String provider) {
        String providerUserId;
        String username;
        String email;
        String nickname;
        String avatar;
        
        // 根据不同的OAuth2提供者提取用户信息
        switch (provider.toLowerCase()) {
            case "github":
                providerUserId = String.valueOf(oauth2User.getAttribute("id"));
                username = oauth2User.getAttribute("login");
                email = oauth2User.getAttribute("email");
                nickname = oauth2User.getAttribute("name");
                avatar = oauth2User.getAttribute("avatar_url");
                break;
                
            case "google":
                providerUserId = oauth2User.getAttribute("sub");
                username = oauth2User.getAttribute("email");
                email = oauth2User.getAttribute("email");
                nickname = oauth2User.getAttribute("name");
                avatar = oauth2User.getAttribute("picture");
                break;
                
            default:
                throw new IllegalArgumentException("不支持的OAuth2提供者: " + provider);
        }
        
        // 查找现有的OAuth2账号绑定
        OAuth2Account existingOAuth2Account = oAuth2AccountService.findByProviderAndProviderUserId(provider, providerUserId);
        
        if (existingOAuth2Account != null) {
            // 更新OAuth2账号信息
            existingOAuth2Account.setProviderUsername(username);
            existingOAuth2Account.setProviderEmail(email);
            existingOAuth2Account.setProviderAvatar(avatar);
            existingOAuth2Account.setUpdatedTime(LocalDateTime.now());
            oAuth2AccountService.updateById(existingOAuth2Account);
            
            // 返回关联的用户
            return userService.getById(existingOAuth2Account.getUserId());
        }
        
        // 尝试根据邮箱查找现有用户
        User existingUser = null;
        if (StringUtils.hasText(email)) {
            existingUser = userService.findByEmail(email);
        }
        
        if (existingUser != null) {
            // 为现有用户创建OAuth2账号绑定
            createOAuth2Account(existingUser.getId(), oauth2User, provider, providerUserId, username, email, avatar);
            return existingUser;
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(generateUniqueUsername(username, provider));
        newUser.setEmail(email);
        newUser.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        newUser.setAvatar(avatar);
        newUser.setPassword(EncryptUtils.encryptPassword(EncryptUtils.generateRandomString(16))); // 随机密码
        newUser.setStatus(User.STATUS_ENABLED);
        newUser.setEmailVerified(StringUtils.hasText(email) ? User.EMAIL_VERIFIED : User.EMAIL_NOT_VERIFIED);
        newUser.setPhoneVerified(User.PHONE_NOT_VERIFIED);
        newUser.setCreatedTime(LocalDateTime.now());
        newUser.setUpdatedTime(LocalDateTime.now());
        
        userService.save(newUser);
        
        // 分配默认角色
        userService.assignDefaultRole(newUser.getId());
        
        // 创建OAuth2账号绑定
        createOAuth2Account(newUser.getId(), oauth2User, provider, providerUserId, username, email, avatar);
        
        log.info("OAuth2新用户注册成功: username={}, provider={}", newUser.getUsername(), provider);
        
        return newUser;
    }

    /**
     * 创建OAuth2账号绑定
     */
    /*
    private void createOAuth2Account(Long userId, OAuth2User oauth2User, String provider, 
                                   String providerUserId, String username, String email, String avatar) {
        OAuth2Account oAuth2Account = new OAuth2Account();
        oAuth2Account.setUserId(userId);
        oAuth2Account.setProvider(provider);
        oAuth2Account.setProviderUserId(providerUserId);
        oAuth2Account.setProviderUsername(username);
        oAuth2Account.setProviderEmail(email);
        oAuth2Account.setProviderAvatar(avatar);
        oAuth2Account.setCreatedTime(LocalDateTime.now());
        oAuth2Account.setUpdatedTime(LocalDateTime.now());
        
        oAuth2AccountService.save(oAuth2Account);
    }

    /**
     * 生成唯一用户名 - 临时注释
     */
    /*
    private String generateUniqueUsername(String baseUsername, String provider) {
        String username = baseUsername + "_" + provider;
        
        // 检查用户名是否已存在
        int counter = 1;
        String originalUsername = username;
        while (userService.findByUsername(username) != null) {
            username = originalUsername + "_" + counter++;
        }
        
        return username;
    }

    /**
     * 构建重定向URL - 临时注释
     */
    /*
    private String buildTargetUrl(String accessToken, String refreshToken) {
        return redirectUri + 
               "?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
               "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
               "&token_type=Bearer";
    }
    */
}
