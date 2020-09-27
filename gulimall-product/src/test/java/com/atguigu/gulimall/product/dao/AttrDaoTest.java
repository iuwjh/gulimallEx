package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class AttrDaoTest {
    @Autowired
    private AttrDao attrDao;

    @BeforeEach
    void setup() {
        attrDao.delete(null);
        attrDao.insert(createEntity());
        attrDao.insert(createEntity().setSearchType(0));
    }

    @Test
    void selectSearchAttrIds() throws Exception {
        List<Long> ids = attrDao.selectList(null).stream().map(AttrEntity::getAttrId).collect(Collectors.toList());
        final List<Long> longs = attrDao.selectSearchAttrIds(ids);
        assertThat(longs).isNotNull().hasSize(1);
        attrDao.insert(createEntity());
        ids = attrDao.selectList(null).stream().map(AttrEntity::getAttrId).collect(Collectors.toList());
        assertThat(attrDao.selectSearchAttrIds(ids)).isNotNull().hasSize(2);

    }

    @Test
    void selectSearchAttrIds_EmptyArg() throws Exception {
        final List<Long> longs = attrDao.selectSearchAttrIds(Collections.emptyList());
        assertThat(longs).isNotNull().hasSize(0);
    }

    private AttrEntity createEntity() {
        return new AttrEntity().setSearchType(1);
    }
}
