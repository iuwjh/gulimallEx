package com.atguigu.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.optional\\..*"))
@Configuration
@PropertySource("classpath:base.properties")
public class GulimallCommonModule {
}
