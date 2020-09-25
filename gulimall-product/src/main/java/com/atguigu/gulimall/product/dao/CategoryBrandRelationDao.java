package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.app.AttrAttrgroupRelationController;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌分类关联
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-11-17 21:25:25
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateCategoryName(@Param("catId") Long catId, @Param("name") String name);

    default int updateByBrandId(long brandId, CategoryBrandRelationEntity relationEntity) {
        return this.update(relationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    default List<CategoryBrandRelationEntity> getByCatelogId(Long catId){
        return this.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }
}
