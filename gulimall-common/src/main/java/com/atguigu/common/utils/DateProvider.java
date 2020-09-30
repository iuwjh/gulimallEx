package com.atguigu.common.utils;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DateProvider {
    /**
     * @see Date#Date()
     */
    public Date now() {
        return new Date();
    }

    /**
     * @see System#currentTimeMillis()
     */
    public long nowInMillis() {
        return System.currentTimeMillis();
    }


}
