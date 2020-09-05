package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SkuItemVo {
    private SkuInfoEntity info;
    private boolean hasStock = true;
    private List<SkuImagesEntity> images;
    private SpuInfoDescEntity desp;
    private List<SpuItemAttrGroupVo> groupAttrs;
    private List<SkuItemSaleAttrVo> saleAttr;

    private SeckillInfoVo seckillInfo;
}
