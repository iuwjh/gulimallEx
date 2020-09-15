package com.atguigu.gulimall.authServer.config;

import com.atguigu.gulimall.authServer.GulimallAuthServerApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallAuthServerApplication.class)
@Configuration
public class FeignConfig {
}
