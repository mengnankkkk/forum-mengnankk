package com.mengnankk.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableFeignClients
@EnableTransactionManagement
@EnableDiscoveryClient
@MapperScan("com.mengnankk.auth.mapper")
public class ForumAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumAuthApplication.class, args);
    }
}
