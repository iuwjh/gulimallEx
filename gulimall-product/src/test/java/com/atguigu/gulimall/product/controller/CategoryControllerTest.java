package com.atguigu.gulimall.product.controller;

import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.product.app.CategoryController;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
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

import java.util.Arrays;
import java.util.List;

import static com.atguigu.common.Rmatcher.Rm;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = CategoryController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class CategoryControllerTest extends ControllerTestBase {
    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/product/category";

    @Test
    void update() throws Exception {
        final CategoryEntity categoryEntity = new CategoryEntity().setCatId(33L);
        mockMvc.perform(get(BASE_URL + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(categoryEntity)))
            .andExpect(Rm().statusEquals(200));
        Mockito.verify(categoryService, Mockito.times(1)).updateCascade(categoryEntity);
    }

    @Test
    void delete() throws Exception {
        final List<Long> catIds = Arrays.asList(4L, 5L);
        mockMvc.perform(get(BASE_URL + "/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(catIds)))
            .andExpect(Rm().statusEquals(200));
        Mockito.verify(categoryService, Mockito.times(1)).removeMenuByIds(catIds);
    }
}
