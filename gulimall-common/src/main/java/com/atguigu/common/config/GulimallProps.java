package com.atguigu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

import java.time.Duration;

/**
 * 全局属性
 */
@Data
@ConfigurationProperties(prefix = "gulimall")
public class GulimallProps {

    public static final String SPRING_PROFILE_DEV = "dev";
    public static final String SPRING_PROFILE_PROD = "prod";
    public static final String SPRING_PROFILE_TEST = "test";

    private String dbName;

    private final ThreadPool threadPool = new ThreadPool();

    private final CorsConfiguration cors = new CorsConfiguration();

    private final Security security = new Security();

    @Data
    public static class ThreadPool {
        private int coreSize = 10;
        private int maxSize = 200;
        private int keepAliveTime = 10;
    }

    @Data
    public static class Security {
        private final Jwt jwt = new Jwt();

        @Data
        public static class Jwt {
            private String secret = null;
            private String base64Secret = null;
            // 默认30分钟
            private long validInMs = Duration.ofMinutes(30).toMillis();
            // 记住我：默认30天
            private long validInMsForRememberMe = Duration.ofDays(30).toMillis();
        }
    }


}
