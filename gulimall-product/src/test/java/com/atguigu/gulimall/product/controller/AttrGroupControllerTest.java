package com.atguigu.gulimall.product.controller;

import com.atguigu.common.controller.ControllerTestBase;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.product.app.AttrGroupController;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
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

@WebMvcTest(controllers = AttrGroupController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class AttrGroupControllerTest extends ControllerTestBase {
    @MockBean
    private AttrGroupService attrGroupService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private AttrAttrgroupRelationService relationService;

    @MockBean
    private AttrService attrService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/product/attrgroup";

    @Test
    void addRelation() throws Exception {
        final long spuId = 2L;
        final List<AttrGroupRelationVo> entities = singletonList(new AttrGroupRelationVo());
        mockMvc.perform(post(BASE_URL + "/attr/relation")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entities)))
            .andExpect(Rm().statusEquals(200));
        Mockito.verify(relationService, Mockito.times(1)).saveBatch(entities);
    }

    @Test
    void getAttrGroupWithAttrs() throws Exception {
        final long catId = 2L;
        final List<AttrAttrgroupRelationEntity> entities = singletonList(new AttrAttrgroupRelationEntity());
        mockMvc.perform(get(BASE_URL + "/{catelogId}/withattr", catId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entities)))
            .andExpect(Rm().RHasKey("data"));
        Mockito.verify(attrGroupService, Mockito.times(1))
            .getAttrGroupWithAttrsByCatelogId(catId);
    }

    @Test
    void attrRelation() throws Exception {
        final long attrId = 2L;
        mockMvc.perform(get(BASE_URL + "/{attrgroupId}/attr/relation", attrId))
            .andExpect(Rm().RHasKey("data"));
        Mockito.verify(attrService, Mockito.times(1))
            .getRelationAttr(attrId);
    }

    @Test
    void attrNoRelation() throws Exception {
        final long attrId = 2L;
        final List<AttrAttrgroupRelationEntity> entities = singletonList(new AttrAttrgroupRelationEntity());
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        mockMvc.perform(get(BASE_URL + "/{attrgroupId}/noattr/relation", attrId)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(params))
            .andExpect(Rm().RHasKey("page"));
        Mockito.verify(attrService, Mockito.times(1))
            .getNoRelationAttr(new HashMap<>(params), attrId);
    }
}
