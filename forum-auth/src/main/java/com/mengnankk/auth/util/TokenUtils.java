package com.mengnankk.auth.util;

import com.mengnankk.auth.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token工具类
 */
@Component
public class TokenUtils {

    @Value("${jwt.secret:ForumJwtSecretKeyForAuthentication}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private Long jwtExpiration; // 默认24小时

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration; // 默认7天

    /**
     * 生成访问Token
     */
    public String generateAccessToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "access");
        return generateToken(claims, username, jwtExpiration * 1000);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return generateToken(claims, username, refreshExpiration * 1000);
    }

    /**
     * 生成Token
     */
    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从Token中获取Token类型
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type").toString();
    }

    /**
     * 从Token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 验证Token并检查类型
     */
    public boolean validateToken(String token, String expectedType) {
        try {
            if (!validateToken(token)) {
                return false;
            }
            String tokenType = getTokenType(token);
            return expectedType.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token已过期");
        } catch (JwtException e) {
            throw new RuntimeException("Token解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String refreshToken) {
        if (!validateToken(refreshToken, "refresh")) {
            throw new TokenExpiredException("刷新Token无效或已过期");
        }

        String username = getUsernameFromToken(refreshToken);
        Long userId = getUserIdFromToken(refreshToken);

        return generateAccessToken(username, userId);
    }

    /**
     * 获取Token剩余过期时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long now = System.currentTimeMillis();
        return (expiration.getTime() - now) / 1000;
    }

    /**
     * 生成Token ID（用于黑名单）
     */
    public String getTokenId(String token) {
        Claims claims = getClaimsFromToken(token);
        return EncryptUtils.md5(claims.getSubject() + claims.getIssuedAt().getTime());
    }
}
