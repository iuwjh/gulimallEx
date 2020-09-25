package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.impl.BrandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandDao brandDao;

    @Mock
    private CategoryBrandRelationService categoryBrandRelationService;

    private BrandService brandService;

    @BeforeEach
    void setup() {
        brandService = Mockito.spy(new BrandServiceImpl(categoryBrandRelationService));
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
}
