package com.mengnankk.auth.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ObjectMapper objectMapper;

    /**
     * 定义切点：所有Controller层的方法
     */
    @Pointcut("execution(* com.mengnankk.auth.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 定义切点：所有Service层的重要方法
     */
    @Pointcut("execution(* com.mengnankk.auth.service..*.register*(..)) || " +
             "execution(* com.mengnankk.auth.service..*.login*(..)) || " +
             "execution(* com.mengnankk.auth.service..*.logout*(..)) || " +
             "execution(* com.mengnankk.auth.service..*.changePassword*(..)) || " +
             "execution(* com.mengnankk.auth.service..*.createRole*(..)) || " +
             "execution(* com.mengnankk.auth.service..*.deleteRole*(..)) || " +
             "execution(* com.mengnankk.auth.service..*.assignRole*(..))")
    public void servicePointcut() {
    }

    /**
     * 环绕通知：记录方法执行时间和参数
     */
    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String methodName = joinPoint.getSignature().getName();
                String className = joinPoint.getTarget().getClass().getSimpleName();
                
                log.info("==> {}#{} [{}] {}", className, methodName, request.getMethod(), request.getRequestURI());
                
                // 记录请求参数（敏感信息需要过滤）
                Object[] args = joinPoint.getArgs();
                if (args.length > 0) {
                    String argsStr = Arrays.stream(args)
                            .map(this::formatArgument)
                            .collect(Collectors.joining(", "));
                    log.debug("请求参数: {}", argsStr);
                }
            }
            
            // 执行方法
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            log.info("<== 响应时间: {}ms", endTime - startTime);
            
            return result;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("<== 执行异常: {}ms, error: {}", endTime - startTime, e.getMessage());
            throw e;
        }
    }

    /**
     * 前置通知：记录Service层重要操作
     */
    @Before("servicePointcut()")
    public void logServiceBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        Object[] args = joinPoint.getArgs();
        String argsStr = Arrays.stream(args)
                .map(this::formatArgument)
                .collect(Collectors.joining(", "));
        
        log.info("SERVICE: {}#{} 开始执行, 参数: {}", className, methodName, argsStr);
    }

    /**
     * 后置返回通知：记录Service层操作成功
     */
    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void logServiceAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.info("SERVICE: {}#{} 执行成功", className, methodName);
    }

    /**
     * 异常通知：记录Service层操作异常
     */
    @AfterThrowing(pointcut = "servicePointcut()", throwing = "ex")
    public void logServiceAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.error("SERVICE: {}#{} 执行异常: {}", className, methodName, ex.getMessage());
    }

    /**
     * 格式化参数，过滤敏感信息
     */
    private String formatArgument(Object arg) {
        if (arg == null) {
            return "null";
        }
        
        String argStr = arg.toString();
        String lowerStr = argStr.toLowerCase();
        
        // 检查是否包含敏感信息
        if (lowerStr.contains("password") || 
            lowerStr.contains("token") || 
            lowerStr.contains("secret") ||
            lowerStr.contains("key")) {
            return "[FILTERED]";
        }
        
        // 对于复杂对象，尝试JSON序列化
        if (arg.getClass().getPackage() != null && 
            arg.getClass().getPackage().getName().startsWith("com.mengnankk")) {
            try {
                return objectMapper.writeValueAsString(arg);
            } catch (Exception e) {
                return arg.getClass().getSimpleName() + "@" + Integer.toHexString(arg.hashCode());
            }
        }
        
        return argStr;
    }
}
