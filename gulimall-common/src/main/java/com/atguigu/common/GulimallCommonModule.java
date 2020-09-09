package com.atguigu.common;

import com.atguigu.common.config.GulimallProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

@Configuration
@EnableConfigurationProperties(GulimallProperties.class)
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.optional\\..*"))
@PropertySource("classpath:base.properties")
public class GulimallCommonModule {

    @Component
    @PropertySource("classpath:dev.properties")
    @Profile("dev")
    public static class ProfileDev {

    }

    @Component
    @PropertySource("classpath:prod.properties")
    @Profile("prod")
    public static class ProfileProd {

    }

    @Component
    @PropertySource("classpath:test.properties")
    @Profile("test")
    public static class ProfileTest {

    }
}
