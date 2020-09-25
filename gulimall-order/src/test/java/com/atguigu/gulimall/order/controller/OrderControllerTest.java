package com.atguigu.gulimall.order.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = OrderController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class OrderControllerTest extends ControllerTestBase {
    @MockBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/order/order";

    @Test
    void cartListPage() throws Exception {
        final String orderSn = "bbb123";
        final OrderEntity orderEntity = new OrderEntity().setOrderSn(orderSn);
        Mockito.when(orderService.getOrderByOrderSn(orderSn)).thenReturn(orderEntity);

        mockMvc.perform(get(BASE_URL + "/status/{orderSn}", orderSn))
            .andExpect(Rm().RDataEquals(orderEntity, new TypeReference<OrderEntity>() {}));
    }

    @Test
    void listWithItem() throws Exception {
        Map<String, Object> params = new HashMap<>();
        final PageUtils page = new PageUtils(new Page<OrderEntity>().setRecords(singletonList(new OrderEntity().setId(8L))));
        Mockito.when(orderService.queryPageWithItem(params)).thenReturn(page);

        mockMvc.perform(post(BASE_URL + "/listWithItem")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(params)))
            .andExpect(Rm().RHasAttr("page", page));
    }
}
