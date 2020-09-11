package com.atguigu.common;

import com.atguigu.common.utils.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.springframework.test.web.servlet.ResultMatcher;

public class Rmatcher {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> ResultMatcher hasKeyValue(String key, T val, Class<T> valClass) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            R r = objectMapper.readValue(json, R.class);
            Assertions.assertThat(r.get(key)).isInstanceOf(valClass);
            Assertions.assertThat(r.get(key)).isEqualToComparingFieldByField(val);
        };
    }


    public static Rmatcher Rm() {
        return new Rmatcher();
    }
}
