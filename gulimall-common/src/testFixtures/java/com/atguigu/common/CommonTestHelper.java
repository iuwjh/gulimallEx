package com.atguigu.common;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public final class CommonTestHelper {
    private CommonTestHelper() {
    }

    public static List<Class<?>> getNonTestClazzIn(String basePackage) throws IOException, ClassNotFoundException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        List<Class<?>> candidates = new ArrayList<Class<?>>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
            ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/" + "**/*.class";
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (!metadataReader.getClassMetadata().getClassName().endsWith("Test")) {
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        }
        return candidates;
    }

    public static MultiValueMap<String, String> mapToMultiValueMap(Map<?, ?> map) {
        LinkedMultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                result.add(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return result;
    }

    public static void clearRedis(RedisTemplate<String, ?> redisTemplate) {
        redisTemplate.delete(Optional.ofNullable(redisTemplate.keys("*")).orElse(Collections.emptySet()));
    }

    public static <T> Comparator<T> treeComparatorBFS(Function<T, List<T>> childrenGetter) {
        return (o1, o2) -> {
            final BreadthFirstIterator<T> iter1 = new BreadthFirstIterator<>(o1, childrenGetter);
            final BreadthFirstIterator<T> iter2 = new BreadthFirstIterator<>(o2, childrenGetter);

            while (iter1.hasNext() && iter2.hasNext()) {
                final List<T> t1Children = childrenGetter.apply(iter1.next());
                final List<T> t2Children = childrenGetter.apply(iter2.next());
                final int sizeDiff = (t1Children == null ? 0 : t1Children.size())
                    - (t2Children == null ? 0 : t2Children.size());
                if (sizeDiff != 0) {
                    return sizeDiff;
                }
            }
            return (iter1.hasNext() ? 1 : 0) - (iter2.hasNext() ? 1 : 0);
        };
    }

    public static <T> List<T> treeFlattenBFS(List<T> root, Function<T, List<T>> childrenGetter) {
        return root.stream().flatMap(c -> treeFlattenBFS(c, childrenGetter).stream()).collect(Collectors.toList());
    }

    public static <T> List<T> treeFlattenBFS(T root, Function<T, List<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        final BreadthFirstIterator<T> iter = new BreadthFirstIterator<>(root, childrenGetter);
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    /**
     * 遍历树的每个节点，把子节点的parentId字段赋值为当前节点Id
     *
     * @param root           树根节点
     * @param childrenGetter 子节点实例getter
     * @param idGetter       父节点id的getter
     * @param parentIdSetter 子节点parentId的getter
     * @param <T>            TreeNode的类型
     * @param <I>            Id的类型
     */
    public static <T, I> void populateParentId(T root, Function<T, List<T>> childrenGetter, Function<T, I> idGetter, BiConsumer<T, I> parentIdSetter) {
        new TreeParentIdPopulator<T, I>(childrenGetter, idGetter, parentIdSetter).popu(root);
    }

    /**
     * 遍历树的每个节点，把子节点的parentId字段赋值为当前节点Id
     *
     * @param <T> TreeNode的类型
     * @param <I> Id的类型
     */
    public static class TreeParentIdPopulator<T, I> {
        private final Function<T, List<T>> childrenGetter;
        private final Function<T, I> idGetter;
        private final BiConsumer<T, I> parentIdSetter;

        public TreeParentIdPopulator(Function<T, List<T>> childrenGetter, Function<T, I> idGetter, BiConsumer<T, I> parentIdSetter) {
            this.childrenGetter = childrenGetter;
            this.idGetter = idGetter;
            this.parentIdSetter = parentIdSetter;
        }

        /**
         * 执行赋值操作
         *
         * @param root
         */
        public void popu(T root) {
            final BreadthFirstIterator<T> iter = new BreadthFirstIterator<>(root, childrenGetter);

            while (iter.hasNext()) {
                T t = iter.next();
                final List<T> tChildren = childrenGetter.apply(t);
                if (!tChildren.isEmpty()) {
                    for (T tChild : tChildren) {
                        parentIdSetter.accept(tChild, idGetter.apply(t));
                    }
                }
            }
        }
    }

    /**
     * 深度优先前序遍历迭代器
     *
     * @param <T> 树节点
     */
    public static class DepthFirstPreOrderIterator<T> implements Iterator<T> {
        private final Function<T, List<T>> childrenGetter;
        private final Deque<Iterator<T>> stack = new ArrayDeque<>();

        public DepthFirstPreOrderIterator(T root, Function<T, List<T>> childrenGetter) {
            this.childrenGetter = childrenGetter;
            stack.addLast(singletonList(root).iterator());
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            Iterator<T> iter = stack.getLast();
            T result = iter.next();
            if (!iter.hasNext()) {
                stack.removeLast();
            }
            Iterator<T> childIter = childrenGetter.apply(result).iterator();
            if (childIter.hasNext()) {
                stack.addLast(childIter);
            }
            return result;
        }
    }

    /**
     * 广度优先迭代器
     *
     * @param <T> 树节点
     */
    public static class BreadthFirstIterator<T> implements Iterator<T> {
        private final Function<T, List<T>> childrenGetter;
        private final Queue<T> queue = new LinkedList<>();

        public BreadthFirstIterator(T root, Function<T, List<T>> childrenGetter) {
            this.childrenGetter = childrenGetter;
            queue.add(root);
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public T next() {
            final T t = queue.remove();
            queue.addAll(childrenGetter.apply(t));
            return t;
        }
    }
}
