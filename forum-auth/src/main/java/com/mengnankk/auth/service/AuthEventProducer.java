package com.mengnankk.auth.service;

import com.mengnankk.auth.constants.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证事件生产者
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventProducer {

    private final RabbitTemplate rabbitTemplate;

    private static final String AUTH_EXCHANGE = "auth.exchange";
    private static final String USER_REGISTER_ROUTING_KEY = "user.register";
    private static final String USER_LOGIN_ROUTING_KEY = "user.login";
    private static final String USER_LOGOUT_ROUTING_KEY = "user.logout";
    private static final String PASSWORD_CHANGE_ROUTING_KEY = "user.password.change";
    private static final String EMAIL_VERIFY_ROUTING_KEY = "user.email.verify";
    private static final String PHONE_VERIFY_ROUTING_KEY = "user.phone.verify";
    private static final String OAUTH2_LOGIN_ROUTING_KEY = "user.oauth2.login";

    /**
     * 发送用户注册事件
     */
    public void sendRegisterEvent(Long userId, String username, String email) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.USER_REGISTER);
        event.put("userId", userId);
        event.put("username", username);
        event.put("email", email);
        
        sendEvent(USER_REGISTER_ROUTING_KEY, event);
        log.info("发送用户注册事件: userId={}, username={}", userId, username);
    }

    /**
     * 发送用户登录事件
     */
    public void sendLoginEvent(Long userId, String username, String loginType) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.USER_LOGIN);
        event.put("userId", userId);
        event.put("username", username);
        event.put("loginType", loginType);
        
        sendEvent(USER_LOGIN_ROUTING_KEY, event);
        log.debug("发送用户登录事件: userId={}, username={}, loginType={}", userId, username, loginType);
    }

    /**
     * 发送用户登出事件
     */
    public void sendLogoutEvent(Long userId, String username) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.USER_LOGOUT);
        event.put("userId", userId);
        event.put("username", username);
        
        sendEvent(USER_LOGOUT_ROUTING_KEY, event);
        log.debug("发送用户登出事件: userId={}, username={}", userId, username);
    }

    /**
     * 发送密码修改事件
     */
    public void sendPasswordChangeEvent(Long userId, String username) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.PASSWORD_CHANGE);
        event.put("userId", userId);
        event.put("username", username);
        
        sendEvent(PASSWORD_CHANGE_ROUTING_KEY, event);
        log.info("发送密码修改事件: userId={}, username={}", userId, username);
    }

    /**
     * 发送邮箱验证事件
     */
    public void sendEmailVerifyEvent(Long userId, String username, String email) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.EMAIL_VERIFY);
        event.put("userId", userId);
        event.put("username", username);
        event.put("email", email);
        
        sendEvent(EMAIL_VERIFY_ROUTING_KEY, event);
        log.info("发送邮箱验证事件: userId={}, username={}, email={}", userId, username, email);
    }

    /**
     * 发送手机验证事件
     */
    public void sendPhoneVerifyEvent(Long userId, String username, String phone) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.PHONE_VERIFY);
        event.put("userId", userId);
        event.put("username", username);
        event.put("phone", phone);
        
        sendEvent(PHONE_VERIFY_ROUTING_KEY, event);
        log.info("发送手机验证事件: userId={}, username={}, phone={}", userId, username, phone);
    }

    /**
     * 发送OAuth2登录事件
     */
    public void sendOAuth2LoginEvent(Long userId, String username, String provider, String providerUserId) {
        Map<String, Object> event = createBaseEvent(AuthConstants.Events.OAUTH2_LOGIN);
        event.put("userId", userId);
        event.put("username", username);
        event.put("provider", provider);
        event.put("providerUserId", providerUserId);
        
        sendEvent(OAUTH2_LOGIN_ROUTING_KEY, event);
        log.info("发送OAuth2登录事件: userId={}, username={}, provider={}", userId, username, provider);
    }

    /**
     * 创建基础事件对象
     */
    private Map<String, Object> createBaseEvent(String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("timestamp", LocalDateTime.now());
        event.put("source", "auth-service");
        return event;
    }

    /**
     * 发送事件到RabbitMQ
     */
    private void sendEvent(String routingKey, Map<String, Object> event) {
        try {
            rabbitTemplate.convertAndSend(AUTH_EXCHANGE, routingKey, event);
        } catch (Exception e) {
            log.error("发送事件失败: routingKey={}, event={}, error={}", routingKey, event, e.getMessage());
        }
    }
}
