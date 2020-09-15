package com.atguigu.gulimall.ware.config;

import com.atguigu.gulimall.ware.GulimallWareApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallWareApplication.class)
@Configuration
public class FeignConfig {
}
