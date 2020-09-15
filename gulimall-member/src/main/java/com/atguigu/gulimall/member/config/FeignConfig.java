package com.atguigu.gulimall.member.config;

import com.atguigu.gulimall.member.GulimallMemberApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallMemberApplication.class)
@Configuration
public class FeignConfig {
}
