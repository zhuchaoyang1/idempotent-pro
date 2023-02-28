package com.example.provider;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.ann.EnableIdempotent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@SuppressWarnings("all")
@EnableDubbo
@EnableAsync
@Configuration
@EnableIdempotent
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}
