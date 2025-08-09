package com.mengnankk.auth.constants;

/**
 * 认证常量类
 */
public class AuthConstants {

    /** JWT Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /** Token请求头名称 */
    public static final String TOKEN_HEADER = "Authorization";
    
    /** 默认角色名称 */
    public static final String DEFAULT_ROLE = "USER";
    
    /** 管理员角色名称 */
    public static final String ADMIN_ROLE = "ADMIN";
    
    /** 超级管理员角色名称 */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    
    /** 版主角色名称 */
    public static final String MODERATOR_ROLE = "MODERATOR";
    
    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";
    
    /** 账户锁定时间(分钟) */
    public static final int ACCOUNT_LOCK_TIME = 30;
    
    /** 最大登录失败次数 */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;
    
    /** 验证码有效期(分钟) */
    public static final int VERIFICATION_CODE_EXPIRE = 5;
    
    /** Token过期时间(秒) - 24小时 */
    public static final long ACCESS_TOKEN_EXPIRE = 24 * 60 * 60;
    
    /** 刷新Token过期时间(秒) - 7天 */
    public static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60;
    
    /** 密码最小长度 */
    public static final int PASSWORD_MIN_LENGTH = 6;
    
    /** 密码最大长度 */
    public static final int PASSWORD_MAX_LENGTH = 20;
    
    /** 用户名最小长度 */
    public static final int USERNAME_MIN_LENGTH = 3;
    
    /** 用户名最大长度 */
    public static final int USERNAME_MAX_LENGTH = 20;
    
    /** 昵称最大长度 */
    public static final int NICKNAME_MAX_LENGTH = 50;
    
    /** 邮箱最大长度 */
    public static final int EMAIL_MAX_LENGTH = 100;
    
    /** 手机号长度 */
    public static final int PHONE_LENGTH = 11;
    
    /** 头像URL最大长度 */
    public static final int AVATAR_URL_MAX_LENGTH = 255;
    
    /** 简介最大长度 */
    public static final int BIO_MAX_LENGTH = 500;

    /** OAuth2 相关常量 */
    public static class OAuth2 {
        /** GitHub授权URL */
        public static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
        
        /** GitHub Token URL */
        public static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
        
        /** GitHub用户信息URL */
        public static final String GITHUB_USER_URL = "https://api.github.com/user";
        
        /** 授权回调URL */
        public static final String CALLBACK_URL = "/auth/oauth2/callback";
        
        /** State参数有效期(分钟) */
        public static final int STATE_EXPIRE = 10;
    }

    /** 权限相关常量 */
    public static class Permissions {
        /** 用户管理权限 */
        public static final String USER_MANAGE = "user:manage";
        
        /** 角色管理权限 */
        public static final String ROLE_MANAGE = "role:manage";
        
        /** 权限管理权限 */
        public static final String PERMISSION_MANAGE = "permission:manage";
        
        /** 论坛管理权限 */
        public static final String FORUM_MANAGE = "forum:manage";
        
        /** 帖子管理权限 */
        public static final String POST_MANAGE = "post:manage";
        
        /** 评论管理权限 */
        public static final String COMMENT_MANAGE = "comment:manage";
        
        /** 系统配置权限 */
        public static final String SYSTEM_CONFIG = "system:config";
        
        /** 系统监控权限 */
        public static final String SYSTEM_MONITOR = "system:monitor";
    }

    /** 事件类型常量 */
    public static class Events {
        /** 用户注册事件 */
        public static final String USER_REGISTER = "user.register";
        
        /** 用户登录事件 */
        public static final String USER_LOGIN = "user.login";
        
        /** 用户登出事件 */
        public static final String USER_LOGOUT = "user.logout";
        
        /** 密码修改事件 */
        public static final String PASSWORD_CHANGE = "user.password.change";
        
        /** 邮箱验证事件 */
        public static final String EMAIL_VERIFY = "user.email.verify";
        
        /** 手机验证事件 */
        public static final String PHONE_VERIFY = "user.phone.verify";
        
        /** OAuth2登录事件 */
        public static final String OAUTH2_LOGIN = "user.oauth2.login";
    }

    private AuthConstants() {
        // 常量类不允许实例化
    }
}
