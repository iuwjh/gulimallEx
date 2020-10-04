package com.atguigu.common.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class RandomProvider {

    public String alphanumeric(int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }
}
