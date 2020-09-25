package com.atguigu.gulimall.order.controller;

import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.web.PayWebController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static com.atguigu.common.Rmatcher.Rm;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = PayWebController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class PayWebControllerTest extends ControllerTestBase {
    @MockBean
    private AlipayTemplate alipayTemplate;

    @MockBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "";

    @Test
    void toTrade() throws Exception {
        final String response = "abc";
        Mockito.when(alipayTemplate.pay(any())).thenReturn(response);

        final String orderSn = "";
        mockMvc.perform(get(BASE_URL + "/payOrder")
            .param("orderSn", orderSn))
            .andExpect(Rm().contentEquals(response, String.class));
    }
}
