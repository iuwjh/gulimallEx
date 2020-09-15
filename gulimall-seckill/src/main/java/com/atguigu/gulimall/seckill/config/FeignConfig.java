package com.atguigu.gulimall.seckill.config;

import com.atguigu.gulimall.seckill.GulimallSeckillApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackageClasses = GulimallSeckillApplication.class)
@Configuration
public class FeignConfig {
}
