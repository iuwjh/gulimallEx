package com.atguigu.gulimall.cart.dao;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.EmbeddedRedisConfig;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("testWithoutSQL")
@ContextConfiguration(initializers = CartRedisDaoTest.UserInit.class)
public class CartRedisDaoTest {
    /**
     * {@link CartRedisDao}的初始化需要ThreadLocal里的用户数据。本类通过挂钩上下文的初始化，给ThreadLocal传入测试数据
     */
    static class UserInit implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            CartInterceptor.userInfoThreadLocal.set(new UserInfoTo().setUserId(7L).setUserKey("8"));
        }
    }

    @Autowired
    CartRedisDao cartRedisDao;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setup() {
        CommonTestHelper.clearRedis(stringRedisTemplate);
    }

    @Test
    void setCartItem_getCartItem() throws Exception {
        final CartItem cartItem = new CartItem();
        final String skuId = "7";

        cartRedisDao.setCartItem(skuId, cartItem);
        assertThat(cartRedisDao.getCartItem(skuId)).isNotNull().isEqualTo(cartItem);
    }

    @Test
    void remoteCartItem_getAllItems() throws Exception {
        final CartItem cartItem = new CartItem();
        final String skuId = "7";

        assertThat(cartRedisDao.getAllItems()).hasSize(0);
        cartRedisDao.setCartItem(skuId, cartItem);
        assertThat(cartRedisDao.getAllItems()).hasSize(1);
        cartRedisDao.remoteCartItem(skuId);
        assertThat(cartRedisDao.getAllItems()).hasSize(0);
    }

    @Test
    void clear() throws Exception {
        final CartItem cartItem = new CartItem();
        final String skuId = "7";

        assertThat(cartRedisDao.getAllItems()).hasSize(0);
        cartRedisDao.setCartItem(skuId, cartItem);
        assertThat(cartRedisDao.getAllItems()).hasSize(1);
        cartRedisDao.clear();
        assertThat(cartRedisDao.getAllItems()).hasSize(0);
    }
}
