package com.atguigu.gulimall.order.controller;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.ControllerTestBase;
import com.atguigu.common.ControllerTestConfig;
import com.atguigu.gulimall.order.constant.OrderSubmitStatus;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.web.OrderWebController;
import com.atguigu.gulimall.order.web.PayWebController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = OrderWebController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class OrderWebControllerTest extends ControllerTestBase {
    @MockBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "";

    @Test
    void toTrade() throws Exception {
        final OrderConfirmVo confirmVo = new OrderConfirmVo().setIntegration(3000).setAddress(singletonList(new MemberAddressVo().setId(6L)));
        Mockito.when(orderService.confirmOrder()).thenReturn(confirmVo);

        mockMvc.perform(get(BASE_URL + "/toTrade"))
            .andExpect(MockMvcResultMatchers.model().attribute("orderConfirmData", confirmVo))
            .andExpect(MockMvcResultMatchers.view().name("confirm"));
    }

    @Test
    void submitOrder() throws Exception {
        final OrderSubmitVo orderSubmitVo = new OrderSubmitVo();
        final SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo().setStatus(OrderSubmitStatus.OK).setOrder(new OrderEntity().setOrderSn("eee333"));
        Mockito.when(orderService.submitOrder(orderSubmitVo)).thenReturn(submitOrderResponseVo);


        mockMvc.perform(post(BASE_URL + "/submitOrder")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(orderSubmitVo, Map.class))))
            .andExpect(MockMvcResultMatchers.model().attribute("submitOrderResp", submitOrderResponseVo))
            .andExpect(MockMvcResultMatchers.view().name("pay"));
    }

    @Test
    void submitOrderNotOK() throws Exception {
        final OrderSubmitVo orderSubmitVo = new OrderSubmitVo();
        final SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo().setStatus(OrderSubmitStatus.INFO_OUTDATED);
        Mockito.when(orderService.submitOrder(orderSubmitVo)).thenReturn(submitOrderResponseVo);


        mockMvc.perform(post(BASE_URL + "/submitOrder")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(orderSubmitVo, Map.class))))
            .andExpect(Rm().flashmapHasKey("msg"))
            .andExpect(Rm().redirectedTo("http://order.gulimall.com/toTrade"));
    }
}
