package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.impl.CategoryBrandRelationServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CategoryBrandRelationServiceTest {

    @Mock
    private BrandDao brandDao;

    @Mock
    private CategoryDao categoryDao;

    @Mock
    private CategoryBrandRelationDao relationDao;

    @Mock
    private BrandService brandService;

    private CategoryBrandRelationService categoryBrandRelationService;

    @BeforeEach
    void setup() {
        categoryBrandRelationService = Mockito.spy(new CategoryBrandRelationServiceImpl(brandDao, categoryDao, relationDao, brandService));
        ReflectionTestUtils.setField(categoryBrandRelationService, "baseMapper", relationDao);
    }

    @Test
    void saveDetail() throws Exception {
        final CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity()
            .setBrandId(3L).setCatelogId(8L).setBrandName("b1").setCatelogName("c1");
        Mockito.when(brandDao.selectById(relationEntity.getBrandId()))
            .thenReturn(new BrandEntity().setName(relationEntity.getBrandName()));
        Mockito.when(categoryDao.selectById(relationEntity.getCatelogId()))
            .thenReturn(new CategoryEntity().setName(relationEntity.getCatelogName()));

        categoryBrandRelationService.saveDetail(relationEntity.toBuilder().brandName(null).catelogName(null).build());
        Mockito.verify(categoryBrandRelationService, Mockito.times(1)).save(relationEntity);
    }

    @Test
    void updateBrand() throws Exception {
        final CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity()
            .setBrandId(8L).setBrandName("b1").setCatelogName("c1");

        categoryBrandRelationService.updateBrandName(relationEntity.getBrandId(), relationEntity.getBrandName());
        Mockito.verify(relationDao, Mockito.times(1)).updateByBrandId(relationEntity.getBrandId(),
            new CategoryBrandRelationEntity().setBrandId(relationEntity.getBrandId()).setBrandName(relationEntity.getBrandName()));
    }

    @Test
    void getBrandsByCatId() throws Exception {
        final BrandEntity brandEntity = new BrandEntity();
        final CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        Mockito.when(relationDao.getByCatelogId(relationEntity.getCatelogId())).thenReturn(singletonList(relationEntity));
        Mockito.when(brandService.getById(relationEntity.getBrandId())).thenReturn(brandEntity);

        final List<BrandEntity> result = categoryBrandRelationService.getBrandsByCatId(relationEntity.getCatelogId());
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull().isEqualTo(brandEntity);
    }
}
