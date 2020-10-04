package com.atguigu.common.anno;

import com.atguigu.common.config.RedisConfig;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.common.utils.GulimallCommonUtilsPackage;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;



@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataRedisTest
@ActiveProfiles({"redisEmbed"})
@Import({RedisConfig.class})
@ComponentScans({
    @ComponentScan(basePackageClasses = GulimallCommonUtilsPackage.class)
})
public @interface GulimallRedisTest {

    @AliasFor(annotation = Import.class, attribute = "value")
    Class<?> daoClass();
}
