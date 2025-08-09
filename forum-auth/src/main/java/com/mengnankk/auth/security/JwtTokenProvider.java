package com.mengnankk.auth.security;

import com.mengnankk.auth.util.TokenUtils;
import com.mengnankk.auth.util.RedisKeys;
import com.mengnankk.auth.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token 提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final TokenUtils tokenUtils;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成认证Token
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // 假设CustomUserDetailsService返回的UserDetails包含userId
        Long userId = null;
        if (userDetails instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) userDetails).getUserId();
        }
        
        return tokenUtils.generateAccessToken(username, userId);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        Long userId = null;
        if (userDetails instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) userDetails).getUserId();
        }
        
        String refreshToken = tokenUtils.generateRefreshToken(username, userId);
        
        // 将刷新Token存储到Redis
        String redisKey = RedisKeys.REFRESH_TOKEN + userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);
        
        return refreshToken;
    }

    /**
     * 从Token获取用户名
     */
    public String getUsernameFromToken(String token) {
        return tokenUtils.getUsernameFromToken(token);
    }

    /**
     * 从Token获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return tokenUtils.getUserIdFromToken(token);
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            if (!tokenUtils.validateToken(token)) {
                return false;
            }
            
            // 检查Token是否在黑名单中
            String tokenId = tokenUtils.getTokenId(token);
            String blacklistKey = RedisKeys.TOKEN_BLACKLIST + tokenId;
            return !redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Token获取认证信息
     */
    public Authentication getAuthentication(String token) {
        try {
            String username = getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            return new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
        } catch (Exception e) {
            log.error("获取认证信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new TokenExpiredException("刷新Token不能为空");
        }
        
        // 验证刷新Token
        if (!tokenUtils.validateToken(refreshToken, "refresh")) {
            throw new TokenExpiredException("刷新Token无效或已过期");
        }
        
        Long userId = tokenUtils.getUserIdFromToken(refreshToken);
        String redisKey = RedisKeys.REFRESH_TOKEN + userId;
        
        // 检查Redis中的刷新Token是否匹配
        String storedRefreshToken = (String) redisTemplate.opsForValue().get(redisKey);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new TokenExpiredException("刷新Token不匹配");
        }
        
        return tokenUtils.refreshToken(refreshToken);
    }

    /**
     * 注销Token（添加到黑名单）
     */
    public void revokeToken(String token) {
        try {
            String tokenId = tokenUtils.getTokenId(token);
            String blacklistKey = RedisKeys.TOKEN_BLACKLIST + tokenId;
            
            // Token剩余过期时间
            long remainingTime = tokenUtils.getTokenRemainingTime(token);
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(blacklistKey, "revoked", 
                        remainingTime, TimeUnit.SECONDS);
            }
            
            log.info("Token已加入黑名单: {}", tokenId);
        } catch (Exception e) {
            log.error("注销Token失败: {}", e.getMessage());
        }
    }

    /**
     * 注销刷新Token
     */
    public void revokeRefreshToken(Long userId) {
        String redisKey = RedisKeys.REFRESH_TOKEN + userId;
        redisTemplate.delete(redisKey);
        log.info("刷新Token已删除: userId={}", userId);
    }

    /**
     * 获取Token剩余过期时间
     */
    public long getTokenRemainingTime(String token) {
        return tokenUtils.getTokenRemainingTime(token);
    }
}
