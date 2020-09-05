package com.atguigu.gulimall.thirdparty.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.thirdparty.vo.AliBizContentVo;
import org.apache.http.client.utils.DateUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallThirdPartyWebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, AliBizContentVo>() {
            @Override
            public AliBizContentVo convert(String bizContent) {
                return JSON.parseObject(bizContent, new TypeReference<AliBizContentVo>() {
                });
            }
        });
    }
}
