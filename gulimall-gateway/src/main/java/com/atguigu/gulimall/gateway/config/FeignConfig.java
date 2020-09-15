package com.atguigu.gulimall.gateway.config;

import com.atguigu.gulimall.gateway.GulimallGatewayApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallGatewayApplication.class)
@Configuration
public class FeignConfig {
}
