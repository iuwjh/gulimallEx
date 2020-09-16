package com.atguigu.common;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.assertj.core.api.Assertions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.function.Function;

public class Rmatcher {
    private final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    public <T> ResultMatcher hasKeyAndJsonValue(String key, T val) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            R r = objectMapper.readValue(json, R.class);
            Assertions.assertThat(objectMapper.writeValueAsString(r.get(key))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public <T> ResultMatcher hasJsonData(T val, TypeReference<T> typeRef) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            R r = objectMapper.readValue(json, R.class);
            Assertions.assertThat(objectMapper.writeValueAsString(r.getData(typeRef))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public <T> ResultMatcher equalValue(T val, Class<T> cls) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            Object r = objectMapper.readValue(json, cls);
            Assertions.assertThat(r).isNotNull().isEqualTo(val);
        };
    }

    public ResultMatcher hasSize(int size, Class<? extends Collection> cls) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            final Collection collection = objectMapper.readValue(json, cls);
            Assertions.assertThat(collection).hasSize(size);
        };
    }

    public ResultMatcher hasStatus(int status) {
        return mvcResult -> Assertions.assertThat(mvcResult.getResponse().getStatus()).isEqualTo(status);
    }

    public static Rmatcher Rm() {
        return new Rmatcher();
    }
}
