package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.impl.SkuInfoServiceImpl;
import com.atguigu.gulimall.product.vo.SeckillInfoVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrTo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SkuInfoServiceTest {

    @Mock
    private SkuImagesService skuImagesService;

    @Mock
    private SpuInfoDescService spuInfoDescService;

    @Mock
    private AttrGroupService attrGroupService;

    @Mock
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Mock
    private SeckillFeignService seckillFeignService;

    @Mock
    private SkuInfoDao skuInfoDao;

    @Spy
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Spy
    @InjectMocks
    private SkuInfoServiceImpl skuInfoService;

    @BeforeEach
    void setup() {
        // skuInfoService = Mockito.spy(new SkuInfoServiceImpl());
        ReflectionTestUtils.setField(skuInfoService, "baseMapper", skuInfoDao);
    }

    @Test
    void queryPageByCondition() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "2");
        params.put("catelogId", "9");
        params.put("brandId", "6");
        params.put("min", "100");
        params.put("max", "500");
        final SkuInfoEntity skuInfoEntity = new SkuInfoEntity().setSkuId(8L);
        final Page<SkuInfoEntity> entityPage = new Page<SkuInfoEntity>().setRecords(singletonList(skuInfoEntity));
        Mockito.doReturn(entityPage).when(skuInfoService).page(any(), any());

        final PageUtils result = skuInfoService.queryPageByCondition(params);
        assertThat(result.getList()).isNotNull().hasSize(1);
        assertThat(result.getList().get(0)).isNotNull().isEqualTo(skuInfoEntity);
    }

    @Test
    void itemBySkuId() throws Exception {
        final SkuInfoEntity skuInfoEntity = new SkuInfoEntity().setSkuId(7L).setSpuId(9L);
        final SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity().setSpuId(skuInfoEntity.getSpuId());
        final SpuItemAttrGroupVo spuItemAttrGroupVo = new SpuItemAttrGroupVo().setGroupName("g1");
        final SkuItemSaleAttrTo skuItemSaleAttrTo = new SkuItemSaleAttrTo().setAttrId(12L).setSkuIds("31,41");
        final SkuImagesEntity skuImagesEntity = new SkuImagesEntity().setSkuId(skuInfoEntity.getSkuId());
        final SeckillInfoVo seckillInfoVo = new SeckillInfoVo().setSkuId(skuInfoEntity.getSkuId());
        Mockito.when(skuInfoDao.selectById(skuInfoEntity.getSkuId())).thenReturn(skuInfoEntity);
        Mockito.when(spuInfoDescService.getById(skuInfoEntity.getSpuId())).thenReturn(spuInfoDescEntity);
        Mockito.when(attrGroupService.getAttrGroupWithAttrsBySpuId(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId()))
            .thenReturn(singletonList(spuItemAttrGroupVo));
        Mockito.when(skuSaleAttrValueDao.getBySpuId(skuInfoEntity.getSpuId())).thenReturn(singletonList(skuItemSaleAttrTo));
        Mockito.when(skuImagesService.getImagesBySkuId(skuInfoEntity.getSkuId())).thenReturn(singletonList(skuImagesEntity));
        Mockito.when(seckillFeignService.getSkuSeckillInfo(skuInfoEntity.getSkuId())).thenReturn(R.ok().setData(seckillInfoVo));

        final SkuItemVo result = skuInfoService.item(skuInfoEntity.getSkuId());
        assertThat(result).isNotNull();
    }
}
