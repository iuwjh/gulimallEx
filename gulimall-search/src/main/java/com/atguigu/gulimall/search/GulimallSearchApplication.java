package com.atguigu.gulimall.search;

import com.atguigu.common.GulimallCommonModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients(basePackages = "com.atguigu.gulimall.search.feign")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackageClasses = {GulimallSearchApplication.class, GulimallCommonModule.class}, exclude = DataSourceAutoConfiguration.class)
public class GulimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }

}
