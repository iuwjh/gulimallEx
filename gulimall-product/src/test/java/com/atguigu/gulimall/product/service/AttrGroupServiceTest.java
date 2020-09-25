package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.impl.AttrGroupServiceImpl;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AttrGroupServiceTest {

    @Mock
    private AttrService attrService;

    @Mock
    private AttrGroupDao attrGroupDao;

    private AttrGroupService attrGroupService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        attrGroupService = Mockito.spy(new AttrGroupServiceImpl(attrService, attrGroupDao));
        ReflectionTestUtils.setField(attrGroupService, "baseMapper", attrGroupDao);
    }

    @Test
    void queryPageCat() throws Exception {
        final AttrGroupEntity attrGroupEntity = new AttrGroupEntity().setCatelogId(7L);
        final Page<AttrGroupEntity> entityPage = new Page<AttrGroupEntity>().setRecords(singletonList(attrGroupEntity));
        Mockito.doReturn(entityPage).when(attrGroupService).page(any(), any());

        Map<String, Object> params = new HashMap<>();
        params.put("key", "88");
        final PageUtils result = attrGroupService.queryPage(params, attrGroupEntity.getCatelogId());
        assertThat(result.getList()).isNotNull().hasSize(1);
        assertThat(result.getList().get(0)).isNotNull().isEqualTo(attrGroupEntity);
    }

    @Test
    void getAttrGroupWithAttrsByCatelogId() throws Exception {
        final AttrGroupEntity attrGroupEntity = new AttrGroupEntity().setAttrGroupId(9L).setCatelogId(7L);
        final AttrEntity attrEntity = new AttrEntity().setAttrId(11L);
        final AttrGroupWithAttrsVo groupWithAttrsVo = objectMapper.convertValue(attrGroupEntity, AttrGroupWithAttrsVo.class)
            .setAttrs(singletonList(attrEntity));
        Mockito.when(attrGroupDao.listByCatalogId(attrGroupEntity.getCatelogId())).thenReturn(singletonList(attrGroupEntity));
        Mockito.when(attrService.getRelationAttr(attrGroupEntity.getAttrGroupId()))
            .thenReturn(groupWithAttrsVo.getAttrs());

        final List<AttrGroupWithAttrsVo> result = attrGroupService.getAttrGroupWithAttrsByCatelogId(attrGroupEntity.getCatelogId());
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull().isEqualTo(groupWithAttrsVo);
    }

}
