package com.atguigu.gulimall.order.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

// @Configuration
public class MySeataConfig {
    //
    // @Bean
    // public DataSource dataSource(DataSourceProperties properties) {
    //     HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    //
    //
    //     if (StringUtils.hasText(properties.getName())) {
    //         dataSource.setPoolName(properties.getName());
    //     }
    //
    //     return new DataSourceProxy(dataSource);
    // }
}
