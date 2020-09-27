package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class SpuInfoDaoTest {
    @Autowired
    SpuInfoDao spuInfoDao;

    @BeforeEach
    void setup() {
        spuInfoDao.delete(null);
        spuInfoDao.insert(createEntity());
    }

    @Test
    void updateSpuStatus() {
        final SpuInfoEntity entity = spuInfoDao.selectList(null).get(0);
        assertThat(entity).isNotNull().isEqualTo(createEntity().setId(entity.getId()));
        spuInfoDao.updateSpuPublishStatus(entity.getId(), 1);
        assertThat(spuInfoDao.selectList(null).get(0).setUpdateTime(null)).isNotNull().isEqualTo(createEntity().setId(entity.getId()).setPublishStatus(1));
    }

    private SpuInfoEntity createEntity() {
        return new SpuInfoEntity().setPublishStatus(0);
    }
}
