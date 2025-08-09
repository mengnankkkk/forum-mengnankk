package com.mengnankk.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ForumSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumSearchApplication.class, args);
    }

}
