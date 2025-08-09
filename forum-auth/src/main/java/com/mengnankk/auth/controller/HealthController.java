package com.mengnankk.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务健康检查控制器
 */
@RestController
@RequestMapping("/actuator")
public class HealthController {

    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    public HealthController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", applicationName);
        health.put("port", serverPort);
        health.put("timestamp", LocalDateTime.now());
        
        // 检查服务注册状态
        try {
            int instanceCount = discoveryClient.getInstances(applicationName).size();
            health.put("nacos", Map.of(
                "status", instanceCount > 0 ? "UP" : "DOWN",
                "instances", instanceCount
            ));
        } catch (Exception e) {
            health.put("nacos", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
        
        return health;
    }

    /**
     * 服务信息端点
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", Map.of(
            "name", applicationName,
            "port", serverPort,
            "version", "1.0.0",
            "description", "Forum认证服务"
        ));
        
        info.put("build", Map.of(
            "timestamp", LocalDateTime.now(),
            "java.version", System.getProperty("java.version")
        ));
        
        return info;
    }

    /**
     * Nacos服务发现信息
     */
    @GetMapping("/nacos")
    public Map<String, Object> nacos() {
        Map<String, Object> nacosInfo = new HashMap<>();
        
        try {
            // 获取所有服务
            nacosInfo.put("services", discoveryClient.getServices());
            
            // 获取当前服务实例
            nacosInfo.put("instances", discoveryClient.getInstances(applicationName));
            
            nacosInfo.put("status", "UP");
        } catch (Exception e) {
            nacosInfo.put("status", "DOWN");
            nacosInfo.put("error", e.getMessage());
        }
        
        return nacosInfo;
    }
}
