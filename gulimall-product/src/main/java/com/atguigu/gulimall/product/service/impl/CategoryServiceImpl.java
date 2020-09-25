package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final CategoryBrandRelationService categoryBrandRelationService;

    private final CategoryDao categoryDao;

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
        List<CategoryEntity> entities = categoryDao.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类（父分类id为0）
        List<CategoryEntity> level1Menus = entities.stream()
            .filter(item -> item.getParentCid() == 0)
            .peek(item -> item.setChildren(getChildrens(item, entities)))
            .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
            .collect(Collectors.toList());

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


        return parentPath.toArray(new Long[0]);
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
        categoryBrandRelationService.updateCategoryName(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories...");
        List<CategoryEntity> entities = categoryDao.listByParentCid(0L);
        return entities;
    }

    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("getCatalogJson...");
        List<CategoryEntity> selectList = categoryDao.selectList(null);

        // 获取1级分类
        List<CategoryEntity> level1Categories = getByParentCid(selectList, 0L);

        // 获取2级分类
        Map<String, List<Catalog2Vo>> catIdToLv2Categories = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            // 获取当前1级分类下的2级分类
            List<CategoryEntity> level2Categories = getByParentCid(selectList, l1.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (level2Categories != null) {
                catalog2Vos = level2Categories.stream().map(l2 -> {
                    Catalog2Vo vo2 = new Catalog2Vo(l1.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3Catalog = getByParentCid(selectList, l2.getCatId());
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

        return catIdToLv2Categories;
    }

    private List<CategoryEntity> getByParentCid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
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
    private List<CategoryEntity> getChildrens(CategoryEntity parent, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream()
            // 只处理parent菜单的子菜单
            .filter(item -> item.getParentCid().equals(parent.getCatId()))
            // 把这些子菜单的引用保存到parent中，再递归查找子菜单的子菜单
            .peek(item -> item.setChildren(getChildrens(item, all)))
            // 根据sort字段对子菜单排序
            .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
            .collect(Collectors.toList());

        return children;
    }


}
