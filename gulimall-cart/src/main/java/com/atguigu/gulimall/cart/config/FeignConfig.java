package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.GulimallCartApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallCartApplication.class)
@Configuration
public class FeignConfig {
}
