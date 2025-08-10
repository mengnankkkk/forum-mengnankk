package com.mengnankk.post;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.mengnankk.post.mapper")
public class ForumPostApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumPostApplication.class, args);
    }

}
