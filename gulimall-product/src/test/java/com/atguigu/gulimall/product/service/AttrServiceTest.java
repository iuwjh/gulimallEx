package com.atguigu.gulimall.product.service;

import com.atguigu.common.to.AttrResponseVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.impl.AttrServiceImpl;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AttrServiceTest {

    @Mock
    private AttrAttrgroupRelationDao relationDao;

    @Mock
    private AttrGroupDao attrGroupDao;

    @Mock
    private CategoryService categoryService;

    @Mock
    private AttrDao attrDao;

    @Mock
    private CategoryDao categoryDao;

    private AttrService attrService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        attrService = Mockito.spy(new AttrServiceImpl(relationDao, attrGroupDao, categoryDao, categoryService, attrDao));
        ReflectionTestUtils.setField(attrService, "baseMapper", attrDao);
    }

    @Test
    void saveAttr() throws Exception {
        final AttrVo attrVo = new AttrVo().setAttrId(8L).setAttrGroupId(9L).setAttrType(1);
        final AttrEntity attrEntity = objectMapper.convertValue(attrVo, AttrEntity.class);
        final AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity()
            .setAttrId(attrVo.getAttrId()).setAttrGroupId(attrVo.getAttrGroupId());

        attrService.saveAttr(attrVo);
        Mockito.verify(relationDao, Mockito.times(1)).insert(relationEntity);
    }

    @Test
    void queryBaseAttrPage() throws Exception {
        final AttrEntity attrEntity = new AttrEntity().setAttrId(7L).setCatelogId(8L);
        final AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity().setAttrGroupId(12L);
        final AttrGroupEntity groupEntity = new AttrGroupEntity().setAttrGroupName("g1");
        final CategoryEntity categoryEntity = new CategoryEntity().setName("c1");
        final Page<AttrEntity> entityPage = new Page<AttrEntity>().setRecords(singletonList(attrEntity));

        Mockito.doReturn(entityPage).when(attrService).page(any(), any());
        Mockito.when(relationDao.getOneByAttrId(attrEntity.getAttrId())).thenReturn(relationEntity);
        Mockito.when(attrGroupDao.selectById(relationEntity.getAttrGroupId())).thenReturn(groupEntity);
        Mockito.when(categoryDao.selectById(attrEntity.getCatelogId())).thenReturn(categoryEntity);

        Map<String, Object> params = new HashMap<>();
        params.put("key", "88");
        final PageUtils result = attrService.queryBaseAttrPage(params, attrEntity.getCatelogId(), "base");
        assertThat(result.getList()).isNotNull().hasSize(1);
    }

    @Test
    void getInfo() throws Exception {
        final AttrEntity attrEntity = new AttrEntity().setAttrId(3L).setAttrType(1).setCatelogId(77L);
        final AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity().setAttrGroupId(12L);
        final AttrGroupEntity groupEntity = new AttrGroupEntity().setAttrGroupName("g1");
        final CategoryEntity categoryEntity = new CategoryEntity().setName("c1");
        final AttrRespVo attrRespVo = objectMapper.convertValue(attrEntity, AttrRespVo.class);
        attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
        attrRespVo.setCatelogName(categoryEntity.getName());
        attrRespVo.setGroupName(groupEntity.getAttrGroupName());
        attrRespVo.setCatelogPath(new Long[]{3L, 4L, 5L});
        Mockito.when(relationDao.getOneByAttrId(attrEntity.getAttrId())).thenReturn(relationEntity);
        Mockito.when(attrGroupDao.selectById(relationEntity.getAttrGroupId())).thenReturn(groupEntity);
        Mockito.when(categoryService.findCatelogPath(attrEntity.getCatelogId())).thenReturn(attrRespVo.getCatelogPath());
        Mockito.when(categoryDao.selectById(attrEntity.getCatelogId())).thenReturn(categoryEntity);
        Mockito.when(attrService.getById(attrEntity.getAttrId())).thenReturn(attrEntity);

        final AttrRespVo result = attrService.getInfo(attrEntity.getAttrId());
        assertThat(result).isNotNull().isEqualTo(attrRespVo);
    }

    @Test
    void updateAttr() throws Exception {
        final AttrVo attrVo = new AttrVo().setAttrId(8L).setAttrGroupId(18L).setAttrType(1);
        final AttrEntity attrEntity = objectMapper.convertValue(attrVo, AttrEntity.class);
        final AttrAttrgroupRelationEntity relationEntity = objectMapper.convertValue(attrVo, AttrAttrgroupRelationEntity.class);
        Mockito.when(relationDao.countByAttrId(attrVo.getAttrId())).thenReturn(1);

        attrService.updateAttr(attrVo);
        Mockito.verify(attrService, Mockito.times(1)).updateById(attrEntity);
        Mockito.verify(relationDao, Mockito.times(1)).updateByAttrId(attrVo.getAttrId(), relationEntity);
        Mockito.verify(relationDao, Mockito.times(0)).insert(relationEntity);
    }

    @Test
    void getRelationAttr() throws Exception {
        final AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity().setAttrId(8L).setAttrGroupId(9L);
        final AttrEntity attrEntity = new AttrEntity().setAttrId(relationEntity.getAttrId());
        Mockito.when(relationDao.listByAttrGroupId(relationEntity.getAttrGroupId())).thenReturn(singletonList(relationEntity));
        Mockito.when(attrService.listByIds(singletonList(relationEntity.getAttrId()))).thenReturn(singletonList(attrEntity));

        final List<AttrEntity> result = attrService.getRelationAttr(relationEntity.getAttrGroupId());
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull().isEqualTo(attrEntity);
    }

    @Test
    void deleteRelation() throws Exception {
        final AttrGroupRelationVo relationVo = new AttrGroupRelationVo();
        final AttrAttrgroupRelationEntity relationEntity = objectMapper.convertValue(relationVo, AttrAttrgroupRelationEntity.class);

        attrService.deleteRelation(singletonList(relationVo).toArray(new AttrGroupRelationVo[0]));
        Mockito.verify(relationDao, Mockito.times(1)).deleteBatchByAttrIdAndAttrGroupId(singletonList(relationEntity));
    }

    @Test
    void getNoRelationAttr() throws Exception {
        final AttrGroupEntity attrGroupEntity = new AttrGroupEntity().setAttrGroupId(80L).setCatelogId(66L);
        final AttrEntity attrEntity = new AttrEntity().setAttrId(22L).setAttrName("a1").setCatelogId(attrGroupEntity.getCatelogId());
        final Page<AttrEntity> entityPage = new Page<AttrEntity>().setRecords(singletonList(attrEntity));

        Mockito.when(relationDao.listByAttrGroupIdsIn(singletonList(attrGroupEntity.getAttrGroupId())))
            .thenReturn(singletonList(new AttrAttrgroupRelationEntity().setAttrId(attrEntity.getAttrId())));
        Mockito.when(attrGroupDao.selectById(attrGroupEntity.getAttrGroupId())).thenReturn(attrGroupEntity);
        Mockito.when(attrGroupDao.listByCatalogId(attrGroupEntity.getCatelogId()))
            .thenReturn(singletonList(attrGroupEntity));
        Mockito.doReturn(entityPage).when(attrService).page(any(), any());

        Map<String, Object> params = new HashMap<>();
        params.put("key", "88");
        final PageUtils result = attrService.getNoRelationAttr(params, attrGroupEntity.getAttrGroupId());
        assertThat(result.getList()).isNotNull().hasSize(1);
        assertThat(result.getList().get(0)).isNotNull().isEqualTo(attrEntity);
    }

    @Test
    void getAttrInfo() throws Exception {
        final AttrEntity attrEntity = new AttrEntity().setAttrId(16L);
        final AttrResponseVo attrResponseVo = objectMapper.convertValue(attrEntity, AttrResponseVo.class);
        Mockito.when(attrDao.selectById(attrEntity.getAttrId())).thenReturn(attrEntity);

        final AttrResponseVo result = attrService.getAttrInfo(attrEntity.getAttrId());
        assertThat(result).isNotNull().isEqualTo(attrResponseVo);

    }
}
