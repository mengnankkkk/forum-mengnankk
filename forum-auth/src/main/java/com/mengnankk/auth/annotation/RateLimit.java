package com.mengnankk.auth.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key的前缀
     */
    String key() default "rate_limit";

    /**
     * 限流时间窗口（秒）
     */
    int time() default 60;

    /**
     * 限流次数
     */
    int count() default 10;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 限流提示信息
     */
    String message() default "";

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 根据IP限流
         */
        IP,

        /**
         * 根据用户限流
         */
        USER,

        /**
         * 根据请求方法限流
         */
        METHOD,

        /**
         * 根据IP和请求方法限流
         */
        IP_AND_METHOD
    }
}
