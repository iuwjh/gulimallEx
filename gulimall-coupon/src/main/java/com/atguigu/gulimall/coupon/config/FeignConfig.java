package com.atguigu.gulimall.coupon.config;

import com.atguigu.gulimall.coupon.GulimallCouponApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallCouponApplication.class)
@Configuration
public class FeignConfig {
}
