package com.atguigu.gulimall.product.controller;

import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.product.app.AttrController;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = AttrController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class AttrControllerTest extends ControllerTestBase {
    @MockBean
    private AttrService attrService;

    @MockBean
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/product/attr";

    @Test
    void baseAttrlistforspu() throws Exception {
        final long spuId = 2L;
        mockMvc.perform(get(BASE_URL + "/base/listforspu/{spuId}", spuId))
            .andExpect(Rm().RHasKey("data"));
        Mockito.verify(productAttrValueService, Mockito.times(1)).baseAttrlistforspu(spuId);
    }

    @Test
    void baseAttrList() throws Exception {
        final long catId = 2L;
        final String attrType = "t1";
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        mockMvc.perform(get(BASE_URL + "/{attrType}/list/{catelogId}", attrType, catId)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(params))
            .andExpect(Rm().RHasKey("page"));
        Mockito.verify(attrService, Mockito.times(1))
            .queryBaseAttrPage(new HashMap<>(params), catId, attrType);
    }

    @Test
    void replaceSpuAttr() throws Exception {
        final long spuId = 2L;
        final String attrType = "t1";
        final List<ProductAttrValueEntity> entities = singletonList(new ProductAttrValueEntity());
        mockMvc.perform(post(BASE_URL + "/update/{spuId}", spuId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entities)))
            .andExpect(Rm().statusEquals(200));
        Mockito.verify(productAttrValueService, Mockito.times(1))
            .replaceSpuAttr(spuId, entities);
    }
}
