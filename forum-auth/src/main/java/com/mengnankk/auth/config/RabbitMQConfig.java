package com.mengnankk.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String AUTH_EXCHANGE = "auth.exchange";
    
    // 队列名称
    public static final String USER_REGISTER_QUEUE = "user.register.queue";
    public static final String USER_LOGIN_QUEUE = "user.login.queue";
    public static final String USER_LOGOUT_QUEUE = "user.logout.queue";
    public static final String PASSWORD_CHANGE_QUEUE = "user.password.change.queue";
    public static final String EMAIL_VERIFY_QUEUE = "user.email.verify.queue";
    public static final String PHONE_VERIFY_QUEUE = "user.phone.verify.queue";
    public static final String OAUTH2_LOGIN_QUEUE = "user.oauth2.login.queue";
    
    // 路由键
    public static final String USER_REGISTER_ROUTING_KEY = "user.register";
    public static final String USER_LOGIN_ROUTING_KEY = "user.login";
    public static final String USER_LOGOUT_ROUTING_KEY = "user.logout";
    public static final String PASSWORD_CHANGE_ROUTING_KEY = "user.password.change";
    public static final String EMAIL_VERIFY_ROUTING_KEY = "user.email.verify";
    public static final String PHONE_VERIFY_ROUTING_KEY = "user.phone.verify";
    public static final String OAUTH2_LOGIN_ROUTING_KEY = "user.oauth2.login";

    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    /**
     * 认证相关事件交换机
     */
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
    }

    // ========== 队列定义 ==========

    @Bean
    public Queue userRegisterQueue() {
        return QueueBuilder.durable(USER_REGISTER_QUEUE).build();
    }

    @Bean
    public Queue userLoginQueue() {
        return QueueBuilder.durable(USER_LOGIN_QUEUE).build();
    }

    @Bean
    public Queue userLogoutQueue() {
        return QueueBuilder.durable(USER_LOGOUT_QUEUE).build();
    }

    @Bean
    public Queue passwordChangeQueue() {
        return QueueBuilder.durable(PASSWORD_CHANGE_QUEUE).build();
    }

    @Bean
    public Queue emailVerifyQueue() {
        return QueueBuilder.durable(EMAIL_VERIFY_QUEUE).build();
    }

    @Bean
    public Queue phoneVerifyQueue() {
        return QueueBuilder.durable(PHONE_VERIFY_QUEUE).build();
    }

    @Bean
    public Queue oauth2LoginQueue() {
        return QueueBuilder.durable(OAUTH2_LOGIN_QUEUE).build();
    }

    // ========== 绑定关系 ==========

    @Bean
    public Binding userRegisterBinding() {
        return BindingBuilder.bind(userRegisterQueue())
                .to(authExchange())
                .with(USER_REGISTER_ROUTING_KEY);
    }

    @Bean
    public Binding userLoginBinding() {
        return BindingBuilder.bind(userLoginQueue())
                .to(authExchange())
                .with(USER_LOGIN_ROUTING_KEY);
    }

    @Bean
    public Binding userLogoutBinding() {
        return BindingBuilder.bind(userLogoutQueue())
                .to(authExchange())
                .with(USER_LOGOUT_ROUTING_KEY);
    }

    @Bean
    public Binding passwordChangeBinding() {
        return BindingBuilder.bind(passwordChangeQueue())
                .to(authExchange())
                .with(PASSWORD_CHANGE_ROUTING_KEY);
    }

    @Bean
    public Binding emailVerifyBinding() {
        return BindingBuilder.bind(emailVerifyQueue())
                .to(authExchange())
                .with(EMAIL_VERIFY_ROUTING_KEY);
    }

    @Bean
    public Binding phoneVerifyBinding() {
        return BindingBuilder.bind(phoneVerifyQueue())
                .to(authExchange())
                .with(PHONE_VERIFY_ROUTING_KEY);
    }

    @Bean
    public Binding oauth2LoginBinding() {
        return BindingBuilder.bind(oauth2LoginQueue())
                .to(authExchange())
                .with(OAUTH2_LOGIN_ROUTING_KEY);
    }
}
