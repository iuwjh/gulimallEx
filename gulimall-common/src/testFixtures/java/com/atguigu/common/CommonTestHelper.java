package com.atguigu.common;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.*;

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
            result.add(entry.getKey().toString(), entry.getValue().toString());
        }
        return result;
    }

    public static void clearRedis(RedisTemplate<String, ?> redisTemplate) {
        redisTemplate.delete(Optional.ofNullable(redisTemplate.keys("*")).orElse(Collections.emptySet()));
    }
}
