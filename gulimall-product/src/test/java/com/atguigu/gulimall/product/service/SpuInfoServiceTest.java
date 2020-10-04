package com.atguigu.gulimall.product.service;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.impl.SpuInfoServiceImpl;
import com.atguigu.gulimall.product.vo.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SpuInfoServiceTest {

    @Mock
    private SpuInfoDescService spuInfoDescService;

    @Mock
    private SpuInfoDao spuInfoDao;

    @Mock
    private SpuImagesService imagesService;

    @Mock
    private AttrService attrService;

    @Mock
    private ProductAttrValueService attrValueService;

    @Mock
    private SkuInfoService skuInfoService;

    @Mock
    private SkuImagesService skuImagesService;

    @Mock
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Mock
    private CouponFeignService couponFeignService;

    @Mock
    private BrandService brandService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private WareFeignService wareFeignService;

    @Mock
    private SearchFeignService searchFeignService;

    @Mock
    private DateProvider dateProvider;

    @Spy
    @InjectMocks
    private SpuInfoServiceImpl spuInfoService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        // spuInfoService = Mockito.spy(new SpuInfoServiceImpl());
    }

    @Test
    void saveImages() throws Exception {
        final Date date = new Date();
        final String descriptImg = "descriptImg1";
        final String spuImg = "spuImg1";
        final BaseAttrs baseAttrs = new BaseAttrs().setAttrId(3L).setAttrValues("").setShowDesc(1);
        final Bounds bounds = new Bounds();
        final Images skuImg = new Images().setDefaultImg(1).setImgUrl("skuImg");
        final Skus skus = new Skus().setImages(singletonList(skuImg)).setAttr(singletonList(new Attr())).setFullCount(3).setFullPrice(BigDecimal.valueOf(200));
        final AttrEntity attrEntity = new AttrEntity().setAttrName("a1");
        final SpuSaveVo spuSaveVo = new SpuSaveVo().setSpuName("spu1").setWeight(BigDecimal.valueOf(90)).setBrandId(8L)
            .setCatalogId(19L).setDecriptImgs(singletonList(descriptImg)).setSpuImages(singletonList(spuImg))
            .setBaseAttrs(singletonList(baseAttrs)).setBounds(bounds).setSkus(singletonList(skus));
        final SpuInfoEntity spuInfoEntity = objectMapper.convertValue(spuSaveVo, SpuInfoEntity.class);
        final SpuBoundTo boundTo = objectMapper.convertValue(bounds, SpuBoundTo.class).setSpuId(spuInfoEntity.getId());
        final SkuReductionTo skuReductionTo = objectMapper.convertValue(skus, SkuReductionTo.class);

        Mockito.when(dateProvider.now()).thenReturn(date);
        Mockito.when(attrService.getById(baseAttrs.getAttrId())).thenReturn(attrEntity);
        Mockito.when(couponFeignService.saveSpuBounds(boundTo)).thenReturn(R.ok());
        Mockito.when(couponFeignService.saveSkuReduction(skuReductionTo)).thenReturn(R.ok());

        spuInfoService.saveSpuInfo(spuSaveVo);

        Mockito.verify(spuInfoDao, Mockito.times(1))
            .insert(spuInfoEntity.setCreateTime(date).setUpdateTime(date));
        Mockito.verify(spuInfoDescService, Mockito.times(1))
            .saveSpuInfoDesc(new SpuInfoDescEntity().setSpuId(spuInfoEntity.getId()).setDecript(descriptImg));
        Mockito.verify(imagesService, Mockito.times(1))
            .saveSpuImages(spuInfoEntity.getId(), spuSaveVo.getSpuImages());

    }


    @Test
    void spuUp() throws Exception {
        final SkuInfoEntity skuInfoEntity = new SkuInfoEntity().setSkuId(3L).setSpuId(15L).setBrandId(2L).setCatalogId(9L);
        final ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity().setAttrId(18L);
        final BrandEntity brandEntity = new BrandEntity();
        final SkuHasStockVo skuHasStockVo = new SkuHasStockVo().setHasStock(true).setSkuId(skuInfoEntity.getSkuId());

        Mockito.when(attrService.selectSearchAttrIds(singletonList(productAttrValueEntity.getAttrId())))
            .thenReturn(singletonList(productAttrValueEntity.getAttrId()));
        Mockito.when(skuInfoService.getSkusById(skuInfoEntity.getSpuId())).thenReturn(singletonList(skuInfoEntity));
        Mockito.when(attrValueService.baseAttrlistforspu(skuInfoEntity.getSpuId()))
            .thenReturn(singletonList(productAttrValueEntity));
        Mockito.when(brandService.getById(skuInfoEntity.getBrandId())).thenReturn(brandEntity);
        Mockito.when(wareFeignService.getSkusHasStock(singletonList(skuInfoEntity.getSkuId())))
            .thenReturn(R.ok().setData(singletonList(skuHasStockVo)));
        Mockito.when(categoryService.getById(skuInfoEntity.getCatalogId())).thenReturn(new CategoryEntity());

        Mockito.when(searchFeignService.productStartUp(any())).thenReturn(R.ok());

        spuInfoService.spuUp(skuInfoEntity.getSpuId());
    }
}
