package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.service.impl.SpuImagesServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SpuImagesServiceTest {

    private SpuImagesService spuImagesService;

    @BeforeEach
    void setup() {
        spuImagesService = Mockito.spy(new SpuImagesServiceImpl());
    }

    @Test
    void saveImages() throws Exception {
        final SpuImagesEntity spuImagesEntity = new SpuImagesEntity().setSpuId(8L).setImgUrl("u1");
        Mockito.when(spuImagesService.saveBatch(any())).thenReturn(true);

        spuImagesService.saveSpuImages(spuImagesEntity.getSpuId(), singletonList(spuImagesEntity.getImgUrl()));
        Mockito.verify(spuImagesService, Mockito.times(1)).saveBatch(singletonList(spuImagesEntity));
    }
}
