package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.vo.Attr;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class AttrGroupDaoTest {
    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    AttrDao attrDao;

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    ProductAttrValueDao productAttrValueDao;


    @BeforeEach
    void setup() {
        attrDao.delete(null);
        attrGroupDao.delete(null);
        relationDao.delete(null);
        productAttrValueDao.delete(null);

        final AttrEntity attr = createAttr();
        final AttrGroupEntity attrGroup = createAttrGroup();
        attrDao.insert(attr);
        attrGroupDao.insert(attrGroup);
        relationDao.insert(createAttrRelation().setAttrId(attr.getAttrId()).setAttrGroupId(attrGroup.getAttrGroupId()));
        productAttrValueDao.insert(createProductAttr().setAttrId(attr.getAttrId()));
    }

    @Test
    void getAttrGroupWithAttrsBySpuId() {
        final List<SpuItemAttrGroupVo> result = attrGroupDao.getAttrGroupWithAttrsBySpuId(9L, 3L);
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getGroupName()).isNotNull().isEqualTo("g1");
        assertThat(result.get(0).getAttrs()).containsExactly(new Attr().setAttrName("ak1").setAttrValue("av1"));
    }

    private AttrGroupEntity createAttrGroup() {
        return new AttrGroupEntity().setCatelogId(3L).setAttrGroupName("g1");
    }

    private AttrEntity createAttr() {
        return new AttrEntity().setAttrName("ak1");
    }

    private AttrAttrgroupRelationEntity createAttrRelation() {
        return new AttrAttrgroupRelationEntity().setAttrId(2L);
    }

    private ProductAttrValueEntity createProductAttr() {
        return new ProductAttrValueEntity().setSpuId(9L).setAttrValue("av1");
    }
}
