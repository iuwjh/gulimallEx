package com.atguigu.common;

import com.atguigu.common.config.GulimallProps;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;

@TestConfiguration
@ActiveProfiles("testWithData")
@EnableConfigurationProperties(GulimallProps.class)
public class ControllerTestConfig {
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper mapper) {
        return new MappingJackson2HttpMessageConverter(mapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @PostConstruct
    void initUser(){

    }
}
