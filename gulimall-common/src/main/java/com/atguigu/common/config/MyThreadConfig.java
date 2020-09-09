package com.atguigu.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// @EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Configuration
public class MyThreadConfig {
    @Bean
    // @ConditionalOnMissingBean
    public ThreadPoolExecutor threadPoolExecutor(GulimallProperties config) {
        return new ThreadPoolExecutor(
                config.getThreadPool().getCoreSize(),
                config.getThreadPool().getMaxSize(),
                config.getThreadPool().getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
