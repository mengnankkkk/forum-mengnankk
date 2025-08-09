package com.mengnankk.auth.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;

/**
 * Nacos服务发现配置
 */
@Slf4j
@Configuration
public class NacosConfig implements CommandLineRunner {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    private Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    @PostConstruct
    public void init() {
        try {
            // 获取本机IP地址
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            
            // 设置实例元数据
            nacosDiscoveryProperties.getMetadata().put("startup.time", String.valueOf(System.currentTimeMillis()));
            nacosDiscoveryProperties.getMetadata().put("host.address", hostAddress);
            nacosDiscoveryProperties.getMetadata().put("management.port", String.valueOf(serverPort));
            nacosDiscoveryProperties.getMetadata().put("active.profile", environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default");
            
            log.info("Nacos服务发现配置初始化完成");
            log.info("服务名称: {}", applicationName);
            log.info("服务端口: {}", serverPort);
            log.info("服务IP: {}", hostAddress);
            log.info("Nacos服务器: {}", nacosDiscoveryProperties.getServerAddr());
            log.info("Nacos命名空间: {}", nacosDiscoveryProperties.getNamespace());
            log.info("Nacos分组: {}", nacosDiscoveryProperties.getGroup());
            
        } catch (Exception e) {
            log.error("Nacos配置初始化失败", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // 应用启动完成后打印服务注册信息
        try {
            Thread.sleep(2000); // 等待服务注册完成
            
            log.info("========== 服务注册信息 ==========");
            log.info("服务名称: {}", applicationName);
            log.info("注册状态: 已注册到Nacos");
            log.info("服务实例数: {}", discoveryClient.getInstances(applicationName).size());
            log.info("健康检查: http://localhost:{}/actuator/health", serverPort);
            log.info("服务发现: http://localhost:{}/actuator/nacos", serverPort);
            log.info("================================");
            
        } catch (Exception e) {
            log.error("获取服务注册信息失败", e);
        }
    }
}
