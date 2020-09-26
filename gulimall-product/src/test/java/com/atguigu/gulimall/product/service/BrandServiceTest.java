package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.impl.BrandServiceImpl;
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
public class BrandServiceTest {

    @Mock
    private BrandDao brandDao;

    @Mock
    private CategoryBrandRelationService categoryBrandRelationService;

    @Mock
    private CategoryBrandRelationDao relationDao;

    private BrandService brandService;

    @BeforeEach
    void setup() {
        brandService = Mockito.spy(new BrandServiceImpl(categoryBrandRelationService, relationDao));
        ReflectionTestUtils.setField(brandService, "baseMapper", brandDao);
    }

    @Test
    void updateDetail() throws Exception {
        final BrandEntity brandEntity = new BrandEntity().setName("b1").setBrandId(3L);

        brandService.updateDetail(brandEntity);
        Mockito.verify(brandService, Mockito.times(1)).updateById(brandEntity);
        Mockito.verify(categoryBrandRelationService, Mockito.times(1))
            .updateBrandName(brandEntity.getBrandId(), brandEntity.getName());
    }

    @Test
    void getBrandsByCatId() throws Exception {
        final BrandEntity brandEntity = new BrandEntity();
        final CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        Mockito.when(relationDao.getByCatelogId(relationEntity.getCatelogId())).thenReturn(singletonList(relationEntity));
        Mockito.when(brandService.getById(relationEntity.getBrandId())).thenReturn(brandEntity);

        final List<BrandEntity> result = brandService.getBrandsByCatId(relationEntity.getCatelogId());
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull().isEqualTo(brandEntity);
    }
}
