package com.atguigu.common.config;

import com.atguigu.common.GulimallCommonModule;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallCommonModule.class)
// @Configuration
public class FeignConfig {
}
