package com.atguigu.gulimall.thirdparty.config;

import com.atguigu.gulimall.thirdparty.GulimallThirdPartyApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallThirdPartyApplication.class)
@Configuration
public class FeignConfig {
}
