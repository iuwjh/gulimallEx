package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchByAttrIdAndAttrGroupId(@Param("entities") List<AttrAttrgroupRelationEntity> entities);

    default AttrAttrgroupRelationEntity getOneByAttrId(long attrId) {
        return this.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
    }

    default int countByAttrId(long attrId) {
        return this.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
    }

    default void updateByAttrId(long attrId, AttrAttrgroupRelationEntity relationEntity) {
        this.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
    }

    default List<AttrAttrgroupRelationEntity> listByAttrGroupId(long attrGroupId) {
        return this.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
    }

    default List<AttrAttrgroupRelationEntity> listByAttrGroupIdsIn(List<Long> attrGroupId) {
        return this.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupId));
    }
}
