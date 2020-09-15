package com.atguigu.gulimall.search.config;

import com.atguigu.gulimall.search.GulimallSearchApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallSearchApplication.class)
@Configuration
public class FeignConfig {
}
