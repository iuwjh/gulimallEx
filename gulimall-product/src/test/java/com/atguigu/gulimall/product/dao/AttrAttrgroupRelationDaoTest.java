package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class AttrAttrgroupRelationDaoTest {
    @Autowired
    AttrAttrgroupRelationDao relationDao;


    @BeforeEach
    void setup() {
        relationDao.delete(null);
        relationDao.insert(createEntity());
        relationDao.insert(createEntity().setAttrId(11L).setAttrGroupId(12L));
    }

    @Test
    void deleteBatchByAttrIdAndAttrGroupId() throws Exception {
        assertThat(relationDao.selectList(null)).isNotNull().hasSize(2);
        relationDao.deleteBatchByAttrIdAndAttrGroupId(singletonList(createEntity()));
        assertThat(relationDao.selectList(null)).isNotNull().hasSize(1);
    }

    @Test
    void deleteBatchByAttrIdAndAttrGroupId_EmptyArg() throws Exception {
        assertThat(relationDao.selectList(null)).isNotNull().hasSize(2);
        relationDao.deleteBatchByAttrIdAndAttrGroupId(emptyList());
        assertThat(relationDao.selectList(null)).isNotNull().hasSize(2);
    }

    private AttrAttrgroupRelationEntity createEntity() {
        return new AttrAttrgroupRelationEntity().setAttrId(8L).setAttrGroupId(9L);
    }
}
