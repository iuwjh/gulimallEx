package com.atguigu.gulimall.member;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.EntityContractTestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EntityContractTest {
    @Test
    public void testEqualAndHashcode() throws Exception {
        List<Class<?>> entityClazz = CommonTestHelper.getNonTestClazzIn("com.atguigu.gulimall.member.entity");
        for (Class<?> clazz : entityClazz) {
            EntityContractTestHelper.testEqualAndHashcode(clazz);
        }
    }
}
