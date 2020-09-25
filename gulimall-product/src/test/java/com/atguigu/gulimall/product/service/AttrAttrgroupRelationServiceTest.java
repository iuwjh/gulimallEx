package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.impl.AttrAttrgroupRelationServiceImpl;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AttrAttrgroupRelationServiceTest {

    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        attrAttrgroupRelationService = Mockito.spy(new AttrAttrgroupRelationServiceImpl());
    }

    @Test
    void saveBatch() throws Exception {
        final AttrGroupRelationVo relationVos = new AttrGroupRelationVo().setAttrId(65L).setAttrGroupId(33L);
        final AttrAttrgroupRelationEntity relationEntities = objectMapper.convertValue(relationVos, AttrAttrgroupRelationEntity.class);
        Mockito.when(attrAttrgroupRelationService.saveBatch(anyCollection())).thenReturn(true);

        attrAttrgroupRelationService.saveBatch(singletonList(relationVos));

        Mockito.verify(attrAttrgroupRelationService).saveBatch(singletonList(relationEntities));
    }
}
