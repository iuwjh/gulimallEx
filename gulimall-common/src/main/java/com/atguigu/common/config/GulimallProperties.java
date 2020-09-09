package com.atguigu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

/**
 * 全局属性
 */
@Data
@ConfigurationProperties(prefix = "gulimall")
public class GulimallProperties {

    public static final String SPRING_PROFILE_DEV = "dev";
    public static final String SPRING_PROFILE_PROD = "prod";
    public static final String SPRING_PROFILE_TEST = "test";

    private final ThreadPool threadPool = new ThreadPool();

    private final CorsConfiguration cors = new CorsConfiguration();

    @Data
    public static class ThreadPool {
        private int coreSize = 10;
        private int maxSize = 200;
        private int keepAliveTime = 10;
    }


}
