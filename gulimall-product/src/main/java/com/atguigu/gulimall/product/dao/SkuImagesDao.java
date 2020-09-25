package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku图片
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface SkuImagesDao extends BaseMapper<SkuImagesEntity> {

    default List<SkuImagesEntity> listBySkuId(Long skuId) {
        return this.selectList(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
    }
}
