package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.impl.ProductAttrValueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ProductAttrValueServiceTest {

    @Mock
    private ProductAttrValueDao productAttrValueDao;

    private ProductAttrValueService productAttrValueService;

    @BeforeEach
    void setup() {
        productAttrValueService = Mockito.spy(new ProductAttrValueServiceImpl(productAttrValueDao));
    }

    @Test
    void updateSpuAttr() throws Exception {
        final long spuId = 33L;
        final ProductAttrValueEntity valueEntity = new ProductAttrValueEntity().setAttrName("a1");
        Mockito.when(productAttrValueService.saveBatch(any())).thenReturn(true);

        productAttrValueService.replaceSpuAttr(spuId, singletonList(valueEntity));
        Mockito.verify(productAttrValueDao).deleteBySpuId(spuId);
        Mockito.verify(productAttrValueService, Mockito.times(1)).saveBatch(singletonList(valueEntity));
    }
}
