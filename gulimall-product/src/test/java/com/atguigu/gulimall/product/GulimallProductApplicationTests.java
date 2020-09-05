package com.atguigu.gulimall.product;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;


/**
 * 1、引入oss-starter
 * 2、配置key，endpoint相关信息即可
 * 3、使用OSSClient 进行相关操作
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testDao1() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(13L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    public void testRedisson() {

    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        HashMap<String, List<Catalog2Vo.Catalog3Vo>> map = new HashMap<String, List<Catalog2Vo.Catalog3Vo>>();
        for (int i = 0; i < 100; i++) {
            map.put("a" + i, Collections.singletonList(new Catalog2Vo.Catalog3Vo("1", "2", "嘿嘿")));
        }
        String s = JSON.toJSONString(map);
        opsForValue.set("hello1", s);

        // opsForValue.set("hello", "world_" + UUID.randomUUID().toString());

        // String hello = opsForValue.get("hello");
        // System.out.println(hello);
    }

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }


    @Test
    public void contextLoads() {

//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");

//
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功....");

//        brandService.updateById(brandEntity);


        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach((item) -> {
            System.out.println(item);
        });

    }

}
