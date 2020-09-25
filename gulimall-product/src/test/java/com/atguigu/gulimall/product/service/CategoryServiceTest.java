package com.atguigu.gulimall.product.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.CommonTestHelper;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.impl.CategoryServiceImpl;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private CategoryBrandRelationService categoryBrandRelationService;

    @Mock
    private CategoryDao categoryDao;

    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        categoryService = Mockito.spy(new CategoryServiceImpl(stringRedisTemplate, redissonClient, categoryBrandRelationService, categoryDao));
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryDao);
    }

    @Test
    void listWithTree() throws Exception {
        final List<CategoryEntity> categoriesTree = createCategoryTree();

        // final List<CategoryEntity> categoriesTree = Arrays.asList(categoryRoot1, categoryRoot2);
        final List<CategoryEntity> categoriesFlat = CommonTestHelper.treeFlattenBFS(categoriesTree, CategoryEntity::getChildren);

        Mockito.when(categoryDao.selectList(null)).thenReturn(categoriesFlat);

        final List<CategoryEntity> result = categoryService.listWithTree();
        assertThat(result).isNotNull().hasSize(categoriesTree.size());
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isNotNull().usingComparator(CommonTestHelper.treeComparatorBFS(CategoryEntity::getChildren))
                .isEqualTo(categoriesTree.get(i));
        }
    }

    @Test
    void findCatelogPath() throws Exception {
        final CategoryEntity categoryEntity =
            CategoryEntity.builder().catId(1L).parentCid(0L)
                .child(CategoryEntity.builder().catId(2L).parentCid(1L)
                    .child(CategoryEntity.builder().catId(3L).parentCid(2L).build()).build()).build();
        final List<CategoryEntity> categoriesFlat = CommonTestHelper.treeFlattenBFS(categoryEntity, CategoryEntity::getChildren);
        final List<Long> catIds = categoriesFlat.stream().map(CategoryEntity::getCatId).collect(Collectors.toList());
        for (CategoryEntity entity : categoriesFlat) {
            Mockito.when(categoryService.getById(entity.getCatId())).thenReturn(entity);
        }

        final Long[] result = categoryService.findCatelogPath(catIds.get(catIds.size() - 1));
        assertThat(result).hasSize(3).containsSequence(catIds);
    }

    @Test
    void getCatalogJson() throws Exception {
        final String categoryEntitiesJson = "[{\"catId\":20,\"children\":[{\"catId\":21,\"children\":[],\"name\":\"A\",\"parentCid\":20},{\"catId\":22,\"children\":[{\"catId\":23,\"children\":[],\"name\":\"C\",\"parentCid\":22},{\"catId\":24,\"children\":[],\"name\":\"D\",\"parentCid\":22},{\"catId\":25,\"children\":[],\"name\":\"E\",\"parentCid\":22}],\"name\":\"B\",\"parentCid\":20},{\"catId\":26,\"children\":[{\"catId\":27,\"children\":[],\"name\":\"G\",\"parentCid\":26}],\"name\":\"F\",\"parentCid\":20}],\"name\":\"Root1\",\"parentCid\":0},{\"$ref\":\"$[0].children[0]\"},{\"$ref\":\"$[0].children[1]\"},{\"$ref\":\"$[0].children[2]\"},{\"$ref\":\"$[0].children[1].children[0]\"},{\"$ref\":\"$[0].children[1].children[1]\"},{\"$ref\":\"$[0].children[1].children[2]\"},{\"$ref\":\"$[0].children[2].children[0]\"},{\"catId\":28,\"children\":[{\"catId\":29,\"children\":[{\"catId\":30,\"children\":[],\"name\":\"I\",\"parentCid\":29}],\"name\":\"H\",\"parentCid\":28}],\"name\":\"Root2\",\"parentCid\":0},{\"$ref\":\"$[8].children[0]\"},{\"$ref\":\"$[8].children[0].children[0]\"}]";
        final String resultJson = "{\"20\":[{\"catalog1Id\":\"20\",\"catalog3List\":[],\"id\":\"21\",\"name\":\"A\"},{\"catalog1Id\":\"20\",\"catalog3List\":[{\"catalog2Id\":\"22\",\"id\":\"23\",\"name\":\"C\"},{\"catalog2Id\":\"22\",\"id\":\"24\",\"name\":\"D\"},{\"catalog2Id\":\"22\",\"id\":\"25\",\"name\":\"E\"}],\"id\":\"22\",\"name\":\"B\"},{\"catalog1Id\":\"20\",\"catalog3List\":[{\"catalog2Id\":\"26\",\"id\":\"27\",\"name\":\"G\"}],\"id\":\"26\",\"name\":\"F\"}],\"28\":[{\"catalog1Id\":\"28\",\"catalog3List\":[{\"catalog2Id\":\"29\",\"id\":\"30\",\"name\":\"I\"}],\"id\":\"29\",\"name\":\"H\"}]}";

        // final List<CategoryEntity> categoryEntities = CommonTestHelper.treeFlattenBFS(createCategoryTree(), CategoryEntity::getChildren);
        final List<CategoryEntity> categoryEntities = JSON.parseObject(categoryEntitiesJson, new TypeReference<List<CategoryEntity>>() {});

        Mockito.when(categoryDao.selectList(null))
            .thenReturn(categoryEntities);

        final Map<String, List<Catalog2Vo>> result = categoryService.getCatalogJson();
        assertThat(result).isNotNull().hasSize(2);
        assertThat(JSON.toJSONString(new TreeMap<>(result))).isNotBlank().isEqualTo(resultJson);
    }

    private List<CategoryEntity> createCategoryTree() {
        /*
         * .
         * ├── Root1
         * │   ├── A
         * │   ├── B
         * │   │   ├── C
         * │   │   ├── D
         * │   │   └── E
         * │   └── F
         * │       └── G
         * └── Root2
         *     └── H
         *         └── I
         */
        final List<CategoryEntity> list = Arrays.asList(
            CategoryEntity.builder().name("Root1").catId(20L).parentCid(0L)
                .child(CategoryEntity.builder().name("A").catId(21L).build())
                .child(CategoryEntity.builder().name("B").catId(22L)
                    .child(CategoryEntity.builder().name("C").catId(23L).build())
                    .child(CategoryEntity.builder().name("D").catId(24L).build())
                    .child(CategoryEntity.builder().name("E").catId(25L).build()).build())
                .child(CategoryEntity.builder().name("F").catId(26L)
                    .child(CategoryEntity.builder().name("G").catId(27L).build()).build()).build(),
            CategoryEntity.builder().name("Root2").catId(28L).parentCid(0L)
                .child(CategoryEntity.builder().name("H").catId(29L)
                    .child(CategoryEntity.builder().name("I").catId(30L).build()).build()).build());

        final CommonTestHelper.TreeParentIdPopulator<CategoryEntity, Long> populator =
            new CommonTestHelper.TreeParentIdPopulator<>(CategoryEntity::getChildren, CategoryEntity::getCatId, CategoryEntity::setParentCid);
        list.forEach(populator::popu);

        return list;
    }
}
