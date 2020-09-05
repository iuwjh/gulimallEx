package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Autowired
    SeckillFeignService seckillFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {

            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);

                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusById(Long spuId) {
        List<SkuInfoEntity> skuList = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        return skuList;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVo.setInfo(skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((skuInfo) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuInfo.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((skuInfo) -> {
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(skuInfo.getSpuId(), skuInfo.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((skuInfo) -> {
            List<SkuItemSaleAttrTo> attrTos = skuSaleAttrValueDao.getBySpuId(skuInfo.getSpuId());

            List<SkuItemSaleAttrVo> attrVos = new ArrayList<>();

            attrTos.stream().map(SkuItemSaleAttrTo::getAttrId).distinct().forEach(attrId -> {
                SkuItemSaleAttrVo saleAttrVo = new SkuItemSaleAttrVo();
                saleAttrVo.setAttrId(attrId);
                List<SkuItemSaleAttrVo.AttrValuesVO> attrValuesVOS = new ArrayList<>();
                attrTos.stream().filter(item -> item.getAttrId().equals(attrId)).forEach(attrTo -> {
                    if (saleAttrVo.getAttrName() == null) saleAttrVo.setAttrName(attrTo.getAttrName());
                    SkuItemSaleAttrVo.AttrValuesVO attrValuesVO = new SkuItemSaleAttrVo.AttrValuesVO();
                    attrValuesVO.setVal(attrTo.getAttrValue());
                    attrValuesVO.setChecked(Arrays.stream(attrTo.getSkuIds().split(",")).anyMatch(attrSkuId -> attrSkuId.equals(skuId.toString())));
                    attrValuesVO.setSkuIds(attrTo.getSkuIds());
                    attrValuesVOS.add(attrValuesVO);
                });
                saleAttrVo.setAttrValues(attrValuesVOS);
                attrVos.add(saleAttrVo);
            });

            skuItemVo.setSaleAttr(attrVos);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImages);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo data = r.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(data);
            }

        }, executor);


        CompletableFuture.allOf(baseAttrFuture, descFuture, imageFuture, saleAttrFuture, seckillFuture).get();

        return skuItemVo;
    }
}
