package com.atguigu.gulimall.product.config;

import com.atguigu.gulimall.product.GulimallProductApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallProductApplication.class)
@Configuration
public class FeignConfig {
}
