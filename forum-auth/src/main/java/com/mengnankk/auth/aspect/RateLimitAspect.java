package com.mengnankk.auth.aspect;

import com.mengnankk.auth.annotation.RateLimit;
import com.mengnankk.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 限流前置通知
     */
    @Before("@annotation(rateLimit)")
    public void rateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String key = generateKey(request, rateLimit);
        
        // 获取当前计数
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            // 第一次访问，设置计数为1
            redisTemplate.opsForValue().set(key, 1, rateLimit.time(), TimeUnit.SECONDS);
        } else if (count < rateLimit.count()) {
            // 未达到限制，计数加1
            redisTemplate.opsForValue().increment(key);
        } else {
            // 达到限制，抛出异常
            String message = rateLimit.message().isEmpty() ? 
                    String.format("访问过于频繁，请%d秒后再试", rateLimit.time()) : 
                    rateLimit.message();
            
            log.warn("限流触发: key={}, count={}, limit={}", key, count, rateLimit.count());
            throw new AuthException(message);
        }
    }

    /**
     * 生成限流Key
     */
    private String generateKey(HttpServletRequest request, RateLimit rateLimit) {
        String prefix = "rate_limit:";
        String suffix = "";
        
        switch (rateLimit.limitType()) {
            case IP:
                suffix = getClientIpAddr(request);
                break;
            case USER:
                // 如果有用户信息，使用用户ID，否则使用IP
                String userId = getCurrentUserId();
                suffix = userId != null ? "user_" + userId : getClientIpAddr(request);
                break;
            case METHOD:
                suffix = request.getMethod() + "_" + request.getRequestURI();
                break;
            case IP_AND_METHOD:
                suffix = getClientIpAddr(request) + "_" + request.getMethod() + "_" + request.getRequestURI();
                break;
            default:
                suffix = getClientIpAddr(request);
        }
        
        return prefix + rateLimit.key() + ":" + suffix;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多级代理，获取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            // 这里可以从SecurityContext中获取用户信息
            // SecurityContext context = SecurityContextHolder.getContext();
            // Authentication authentication = context.getAuthentication();
            // if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            //     return ((CustomUserDetails) authentication.getPrincipal()).getUserId().toString();
            // }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
