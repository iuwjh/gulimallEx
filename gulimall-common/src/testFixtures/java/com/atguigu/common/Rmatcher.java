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
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class Rmatcher {
    private final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    /**
     * {@link R}对象中是否存在指定键值对
     * 实现采用JSON字符串序列化
     *
     * @param key
     * @param val
     * @param <T>
     * @return
     */
    public <T> ResultMatcher RHasAttr(String key, T val) {
        return mvcResult -> {
            assertThat(objectMapper.writeValueAsString(mvcResultToR(mvcResult).get(key))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public ResultMatcher RHasKey(String key) {
        return mvcResult -> {
            assertThat(mvcResultToR(mvcResult)).isNotNull().containsKey(key);
        };
    }

    /**
     * {@link R#getData(TypeReference)}中是否存在指定对象
     * 实现采用JSON字符串序列化
     *
     * @param val
     * @param typeRef
     * @param <T>
     * @return
     */
    public <T> ResultMatcher RDataEquals(T val, TypeReference<T> typeRef) {
        return mvcResult -> {
            assertThat(objectMapper.writeValueAsString(mvcResultToR(mvcResult).getData(typeRef))).isNotBlank()
                .isEqualTo(objectMapper.writeValueAsString(val));
        };
    }

    public <T> ResultMatcher contentEquals(T val, Class<T> cls) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            if (!cls.equals(String.class)) {
                Object r = objectMapper.readValue(json, cls);
                assertThat(r).isNotNull().isEqualTo(val);
            } else {
                assertThat(json).isNotNull().isEqualTo((String) val);
            }
        };
    }

    public ResultMatcher contentAsCollHasSize(int size, Class<? extends Collection> cls) {
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

    public ResultMatcher redirectedToWithQueryParams(String url, MultiValueMap<String, String> queryParams) {
        return mvcResult -> {
            final String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
            assertThat(redirectedUrl).isNotNull().isNotBlank();
            final int i = redirectedUrl.indexOf("?");
            assertThat(i).isGreaterThan(0);
            assertThat(redirectedUrl.substring(0, i)).isNotEmpty().isEqualTo(url);
            assertThat(UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams())
                .isNotNull().isEqualTo(queryParams);
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

    /**
     * 断言flashmap本身大小
     *
     * @param size
     * @param <T>
     * @return
     */
    public <T> ResultMatcher flashmapSize(int size) {
        return mvcResult -> {
            assertThat((Map<String, Object>) mvcResult.getFlashMap()).hasSize(size);
        };
    }

    public ResultMatcher flashmapHasKey(String key) {
        return mvcResult -> {
            final Map<String, Object> flashMap = mvcResult.getFlashMap();
            assertThat(flashMap).containsKey(key);
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

    /**
     * 断言flashmap对应key的对象是Map，并且Map大小为指定值
     *
     * @param key
     * @param size
     * @param <T>
     * @return
     */
    public <T> ResultMatcher flashmapMapSize(String key, int size) {
        return mvcResult -> {
            final Object o = mvcResult.getFlashMap().get(key);
            assertThat(o).isNotNull().isInstanceOf(Map.class);
            assertThat(((Map<?, ?>) o).entrySet()).hasSize(size);
        };
    }

    /**
     * 断言flashmap对应key的对象是Collection。并且Collection大小为指定值
     *
     * @param key
     * @param size
     * @param <T>
     * @return
     */
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
