package com.mengnankk.auth.security;

import com.mengnankk.auth.constants.AuthConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = getTokenFromRequest(request);
            
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 记录用户访问
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    log.debug("用户 {} 访问: {}", username, request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("无法设置用户认证: {}", e.getMessage());
            // 清除SecurityContext
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AuthConstants.TOKEN_HEADER);
        
        if (StringUtils.hasText(bearerToken) && 
            bearerToken.startsWith(AuthConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(AuthConstants.TOKEN_PREFIX.length());
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过公开API
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/api/auth/oauth2") ||
               path.startsWith("/api/auth/verify") ||
               path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars");
    }
}
