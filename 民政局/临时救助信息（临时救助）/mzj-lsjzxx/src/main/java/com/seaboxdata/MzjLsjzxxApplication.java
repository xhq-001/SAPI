package com.seaboxdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.seaboxdata")
public class MzjLsjzxxApplication {

    public static void main(String[] args) {
        SpringApplication.run(MzjLsjzxxApplication.class, args);
    }

}
