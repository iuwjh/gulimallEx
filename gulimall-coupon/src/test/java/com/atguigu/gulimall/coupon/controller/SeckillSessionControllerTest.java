package com.atguigu.gulimall.coupon.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.ControllerTestConfig;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = SeckillSessionController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class SeckillSessionControllerTest {
    @MockBean
    private SeckillSessionService seckillSessionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/coupon/seckillsession";

    @Test
    void getLatest3DaySession() throws Exception {
        final List<SeckillSessionEntity> entityList = singletonList(new SeckillSessionEntity());
        Mockito.when(seckillSessionService.getLatest3DaySession()).thenReturn(entityList);

        mockMvc.perform(get(BASE_URL + "/latest3DaySession"))
            .andExpect(Rm().RDataEquals(entityList, new TypeReference<List<SeckillSessionEntity>>(){}));
    }
}
