package com.mengnankk.auth.util;

/**
 * Redis Key管理类
 */
public class RedisKeys {

    /** 用户信息缓存前缀 */
    public static final String USER_INFO = "auth:user:info:";
    
    /** 用户角色缓存前缀 */
    public static final String USER_ROLES = "auth:user:roles:";
    
    /** 用户权限缓存前缀 */
    public static final String USER_PERMISSIONS = "auth:user:permissions:";
    
    /** Token黑名单前缀 */
    public static final String TOKEN_BLACKLIST = "auth:token:blacklist:";
    
    /** 短信验证码前缀 */
    public static final String SMS_CODE = "auth:sms:code:";
    
    /** 邮箱验证码前缀 */
    public static final String EMAIL_CODE = "auth:email:code:";
    
    /** 登录失败计数前缀 */
    public static final String LOGIN_FAIL_COUNT = "auth:login:fail:";
    
    /** 账户锁定前缀 */
    public static final String ACCOUNT_LOCK = "auth:account:lock:";
    
    /** OAuth2 State前缀 */
    public static final String OAUTH2_STATE = "auth:oauth2:state:";
    
    /** 刷新Token前缀 */
    public static final String REFRESH_TOKEN = "auth:refresh:token:";
    
    /** 在线用户前缀 */
    public static final String ONLINE_USER = "auth:online:user:";

    private RedisKeys() {
        // 工具类不允许实例化
    }
}
