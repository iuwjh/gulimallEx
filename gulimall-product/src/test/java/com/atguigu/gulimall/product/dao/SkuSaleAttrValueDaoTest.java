package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrTo;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class SkuSaleAttrValueDaoTest {
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Autowired
    SkuInfoDao skuInfoDao;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        skuInfoDao.delete(null);
        skuSaleAttrValueDao.delete(null);

        final SkuInfoEntity skuInfo = createSkuInfo();
        skuInfoDao.insert(skuInfo);
        skuSaleAttrValueDao.insert(createEntity().setSkuId(skuInfo.getSkuId()));
    }

    @Test
    void getBySpuId() {
        final SkuInfoEntity entity = skuInfoDao.selectList(null).get(0);
        final List<SkuItemSaleAttrTo> result = skuSaleAttrValueDao.getBySpuId(8L);
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull().isEqualTo(objectMapper.convertValue(createEntity(), SkuItemSaleAttrTo.class).setSkuIds(entity.getSkuId().toString()));
    }

    private SkuSaleAttrValueEntity createEntity() {
        return new SkuSaleAttrValueEntity().setAttrId(5L).setAttrName("ak1").setAttrValue("av1");
    }

    private SkuInfoEntity createSkuInfo() {
        return new SkuInfoEntity().setSpuId(8L);
    }
}
