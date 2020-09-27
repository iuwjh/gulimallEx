package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class CategoryBrandRelationDaoTest {
    @Autowired
    CategoryBrandRelationDao relationDao;

    @BeforeEach
    void setup() {
        relationDao.delete(null);
        relationDao.insert(createEntity());
    }

    @Test
    void updateCategoryNameById() {
        final String newName = "b4";
        assertThat(relationDao.selectList(null).get(0).setId(null)).isNotNull().isEqualTo(createEntity());
        relationDao.updateCategoryNameById(createEntity().getCatelogId(), newName);
        assertThat(relationDao.selectList(null).get(0).setId(null)).isNotNull().isEqualTo(createEntity().setCatelogName(newName));
    }

    private CategoryBrandRelationEntity createEntity() {
        return new CategoryBrandRelationEntity().setCatelogId(3L).setCatelogName("b1");
    }
}
