package com.atguigu.common;

import com.atguigu.common.config.GulimallProps;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@EnableConfigurationProperties(GulimallProps.class)
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.optional\\..*"))
public class GulimallCommonModule {

    @Configuration
    @PropertySource("classpath:base.properties")
    @PropertySource("classpath:dev.properties")
    @Profile("dev")
    static class ProfileDev {}

    @Configuration
    @PropertySource("classpath:base.properties")
    @PropertySource("classpath:prod.properties")
    @Profile("prod")
    static class ProfileProd {}

    @Configuration
    @PropertySource("classpath:h2mem.properties")
    @Profile("h2mem")
    static class ProfileH2mem {}

    @Configuration
    @PropertySource("classpath:mysql.properties")
    @Profile("mysql")
    static class ProfileMysql {}

    @Configuration
    @PropertySource("classpath:test-base.properties")
    @Profile("test")
    static class ProfileTest {}

    @Configuration
    @PropertySource("classpath:test-base.properties")
    @PropertySource("classpath:test-with-data.properties")
    @Profile("testWithData")
    static class ProfileTestWithData {}

    @Configuration
    @PropertySource("classpath:test-base.properties")
    @PropertySource("classpath:test-without-sql.properties")
    @Profile("testWithoutSQL")
    static class ProfileTestWithoutSQL {}
}
