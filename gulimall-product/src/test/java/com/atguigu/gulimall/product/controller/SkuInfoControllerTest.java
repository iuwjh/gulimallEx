package com.atguigu.gulimall.product.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.product.app.SkuInfoController;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static com.atguigu.common.Rmatcher.Rm;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = SkuInfoController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class SkuInfoControllerTest extends ControllerTestBase {
    @MockBean
    private SkuInfoService skuInfoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/product/skuinfo";

    @Test
    void getPrice() throws Exception {
        final long skuId = 7L;
        final SkuInfoEntity skuInfoEntity = new SkuInfoEntity().setPrice(BigDecimal.valueOf(500));
        Mockito.when(skuInfoService.getById(skuId)).thenReturn(skuInfoEntity);

        mockMvc.perform(get(BASE_URL + "/{skuId}/price", skuId))
            .andExpect(Rm().RDataEquals(skuInfoEntity.getPrice(), new TypeReference<BigDecimal>() {}));
        Mockito.verify(skuInfoService, Mockito.times(1)).getById(skuId);
    }

}
