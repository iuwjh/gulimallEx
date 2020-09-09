package com.atguigu.common;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
@Slf4j
public class EntityContractTestHelper {
    private EntityContractTestHelper() {
    }

    public static void testEqualAndHashcode(Class<?> c) throws ReflectiveOperationException {
        log.info("testEqualAndHashcode: {}", c.getName());
        Objects.requireNonNull(c);
        Constructor<?> constructor = c.getDeclaredConstructor();
        Object entity1 = constructor.newInstance();
        // 自反性
        assertThat(entity1.toString()).isNotNull();
        assertThat(entity1).isEqualTo(entity1);
        assertThat(entity1.hashCode()).isEqualTo(entity1.hashCode());
        // 不同类型两个实例不等
        Object testObject = new Object();
        assertThat(entity1).isNotEqualTo(testObject);
        assertThat(entity1).isNotEqualTo(null);
        // 同类型两个内容相同的实例相等
        Object entity2 = constructor.newInstance();
        assertThat(entity1).isEqualTo(entity2);
        // 同类型两个内容相同的实例hashcode相等
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }
}
