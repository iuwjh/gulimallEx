package com.atguigu.gulimall.seckill.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.anno.GulimallControllerTest;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

// @WebMvcTest(controllers = SeckillController.class,
//     includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
// )
@GulimallControllerTest(controllers = SeckillController.class)
public class SeckillControllerTest extends ControllerTestBase {
    @MockBean
    private SeckillService seckillService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "";

    @Test
    void getCurrentSeckillSkus() throws Exception {
        final SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo().setSkuId(3L);
        Mockito.when(seckillService.getCurrentSeckillSkus()).thenReturn(singletonList(skuRedisTo));

        mockMvc.perform(get(BASE_URL + "/currentSeckillSkus"))
            .andExpect(Rm().RDataEquals(singletonList(skuRedisTo), new TypeReference<List<SeckillSkuRedisTo>>() {}));
    }

    @Test
    void uploadSeckill() throws Exception {
        mockMvc.perform(get(BASE_URL + "/uploadSeckill"))
            .andExpect(Rm().statusEquals(200));
    }

    @Test
    void getSkuSeckillInfo() throws Exception {
        final SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo().setSkuId(3L);
        Mockito.when(seckillService.getSkuSeckillInfo(skuRedisTo.getSkuId())).thenReturn(skuRedisTo);

        mockMvc.perform(get(BASE_URL + "/sku/seckill/{skuId}", skuRedisTo.getSkuId()))
            .andExpect(Rm().RDataEquals(skuRedisTo, new TypeReference<SeckillSkuRedisTo>() {}));
    }

    @Test
    void seckill() throws Exception {
        final String orderSn = "Nnn";
        final String killId = "kid1";
        final String key = "k1";
        final int num = 4;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("killId", killId);
        params.add("key", key);
        params.add("num", String.valueOf(num));
        Mockito.when(seckillService.tryOrder(killId, key, num)).thenReturn(orderSn);

        mockMvc.perform(get(BASE_URL + "/kill").params(params)
            .sessionAttr(AuthServerConstant.LOGIN_USER, new MemberRespVo()))
            .andExpect(MockMvcResultMatchers.view().name("success"))
            .andExpect(MockMvcResultMatchers.model().attribute("orderSn", orderSn))
        ;
    }
}
