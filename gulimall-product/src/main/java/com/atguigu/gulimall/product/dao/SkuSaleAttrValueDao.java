package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {
    List<SkuItemSaleAttrTo> getBySpuId(@Param("spuId") Long spuId);

    // List<SkuItemSaleAttrVo> getSkuItemSaleAttr(@Param("spuId") Long spuId);
}
