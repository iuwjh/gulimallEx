package com.atguigu.gulimall.product.controller;

import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.product.app.CategoryBrandRelationController;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
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

import java.util.List;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = CategoryBrandRelationController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class CategoryBrandRelationControllerTest extends ControllerTestBase {
    @MockBean
    private CategoryBrandRelationService categoryBrandRelationService;

    @MockBean
    private BrandService brandService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/product/categorybrandrelation";

    @Test
    void addRelation() throws Exception {
        final long brandId = 7L;
        mockMvc.perform(get(BASE_URL + "/catelog/list")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("brandId", String.valueOf(brandId)))
            .andExpect(Rm().RHasKey("data"));
        Mockito.verify(categoryBrandRelationService, Mockito.times(1)).list(any());
    }

    @Test
    void relationBrandsList() throws Exception {
        final long spuId = 2L;
        final List<AttrAttrgroupRelationEntity> entities = singletonList(new AttrAttrgroupRelationEntity());
        final long catId = 7L;
        mockMvc.perform(get(BASE_URL + "/brands/list")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("catId", String.valueOf(catId)))
            .andExpect(Rm().RHasKey("data"));
        Mockito.verify(brandService, Mockito.times(1)).getBrandsByCatId(catId);
    }
}
