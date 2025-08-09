package com.mengnankk.auth.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Nacos关闭钩子，确保优雅关闭Nacos相关线程
 */
@Component
public class NacosShutdownHook {

    private static final Logger log = LoggerFactory.getLogger(NacosShutdownHook.class);

    @PreDestroy
    public void shutdownNacos() {
        log.info("正在关闭Nacos相关组件...");
        try {
            // 关闭Nacos通知中心
            com.alibaba.nacos.common.notify.NotifyCenter.shutdown();
            log.info("Nacos NotifyCenter 已关闭");
        } catch (Exception e) {
            log.warn("关闭Nacos NotifyCenter时出现异常: {}", e.getMessage());
        }
        
        try {
            // 强制关闭所有后台线程
            System.setProperty("nacos.shutdown.hook", "true");
            log.info("已设置Nacos关闭标志");
        } catch (Exception e) {
            log.warn("设置Nacos关闭标志时出现异常: {}", e.getMessage());
        }
        
        log.info("Nacos组件关闭完成");
    }
}
