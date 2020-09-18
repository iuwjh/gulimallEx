package com.atguigu.common;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class Rmatcher {
    private final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    public <T> ResultMatcher RHasAttr(String key, T val) {
        return mvcResult -> {
            assertThat(objectMapper.writeValueAsString(mvcResultToR(mvcResult).get(key))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public <T> ResultMatcher RDataEquals(T val, TypeReference<T> typeRef) {
        return mvcResult -> {
            assertThat(objectMapper.writeValueAsString(mvcResultToR(mvcResult).getData(typeRef))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public <T> ResultMatcher contentEquals(T val, Class<T> cls) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            Object r = objectMapper.readValue(json, cls);
            assertThat(r).isNotNull().isEqualTo(val);
        };
    }

    public ResultMatcher hasSize(int size, Class<? extends Collection> cls) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            final Collection<?> collection = objectMapper.readValue(json, cls);
            assertThat(collection).hasSize(size);
        };
    }

    public ResultMatcher statusEquals(int status) {
        return mvcResult -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(status);
    }

    public ResultMatcher redirectedTo(String url) {
        return mvcResult -> {
            assertThat(mvcResult.getResponse().getRedirectedUrl())
                .isNotNull().isEqualTo(url);
        };
    }

    public <T> ResultMatcher flashmapKeyExist(String key) {
        return mvcResult -> {
            assertThat((Map<String, Object>) mvcResult.getFlashMap()).containsKey(key);
        };
    }

    public <T> ResultMatcher flashmapKeyNotExist(String key) {
        return mvcResult -> {
            assertThat((Map<String, Object>) mvcResult.getFlashMap()).doesNotContainKey(key);
        };
    }

    public <T> ResultMatcher flashmapSize(int size) {
        return mvcResult -> {
            assertThat((Map<String, Object>) mvcResult.getFlashMap()).hasSize(size);
        };
    }

    public <T> ResultMatcher flashmapHasAttr(String key, T val) {
        return mvcResult -> {
            final Map<String, Object> flashMap = mvcResult.getFlashMap();
            assertThat(flashMap).containsKey(key);
            assertThat(objectMapper.writeValueAsString(flashMap.get(key))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public <T> ResultMatcher flashmapMapSize(String key, int size) {
        return mvcResult -> {
            final Object o = mvcResult.getFlashMap().get(key);
            assertThat(o).isNotNull().isInstanceOf(Map.class);
            assertThat(((Map<?, ?>) o).entrySet()).hasSize(size);
        };
    }

    public <T> ResultMatcher flashmapCollSize(String key, int size) {
        return mvcResult -> {
            final Object o = mvcResult.getFlashMap().get(key);
            assertThat(o).isNotNull().isInstanceOf(Collection.class);
            assertThat((Collection<?>) o).hasSize(size);
        };
    }

    private R mvcResultToR(MvcResult mvcResult) throws IOException {
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), R.class);
    }

    public static Rmatcher Rm() {
        return new Rmatcher();
    }
}
