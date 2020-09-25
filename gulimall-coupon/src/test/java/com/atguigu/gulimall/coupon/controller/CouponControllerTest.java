package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = CouponController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
@ImportAutoConfiguration(classes = RefreshAutoConfiguration.class)
public class CouponControllerTest {
    @MockBean
    private CouponService couponService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/coupon/coupon";

    @Test
    void membercoupons() throws Exception {
        mockMvc.perform(get(BASE_URL + "/member/list"))
            .andExpect(Rm().RHasAttr("coupons", singletonList(new CouponEntity().setCouponName("满100减10"))));
    }
}
