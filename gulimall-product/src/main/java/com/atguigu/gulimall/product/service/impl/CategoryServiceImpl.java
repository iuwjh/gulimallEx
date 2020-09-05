package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    // private Map<String, Object> cache = new HashMap<>();

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    // @Caching(evict = {
    //         @CacheEvict(value = "category", key = "'getLevel1Categories'"),
    //         @CacheEvict(value = "category", key = "'getCatalogJson'")
    // })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories...");
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

            return catalogJsonFromDb;
        }

        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return result;
    }

    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("getCatalogJson...");
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);

        Map<String, List<Catalog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo vo2 = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        vo2.setCatalog3List(collect);
                    }
                    return vo2;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return parent_cid;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }

        return dataFromDb;

    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String lockDelScript =
                        "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";
                Long lockDelResult = stringRedisTemplate.execute(
                        new DefaultRedisScript<>(lockDelScript, Long.class), Collections.singletonList("lock"), uuid);
            }

            // String lockVal = stringRedisTemplate.opsForValue().get("lock");
            // if(uuid.equals(lockVal)){
            //     stringRedisTemplate.delete("lock");
            // }


            return dataFromDb;
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // Map<String, List<Catalog2Vo>> catalogJson = (Map<String, List<Catalog2Vo>>) cache.get("catalogJson");
        // if (catalogJson == null) {
        //
        //     cache.put("catalogJson", parent_cid);
        // }
        // return catalogJson;

        synchronized (this) {
            return getDataFromDb();
        }
    }


    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);

        Map<String, List<Catalog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo vo2 = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        vo2.setCatalog3List(collect);
                    }
                    return vo2;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        // return baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


}
