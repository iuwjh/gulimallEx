package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.GulimallOrderApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallOrderApplication.class)
@Configuration
public class FeignConfig {
}
