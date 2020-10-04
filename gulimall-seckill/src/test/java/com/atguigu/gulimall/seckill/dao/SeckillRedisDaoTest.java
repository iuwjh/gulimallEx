package com.atguigu.gulimall.seckill.dao;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.anno.GulimallRedisTest;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.gulimall.seckill.dao.redis.SeckillRedisDao;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PostConstruct;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@GulimallRedisTest(daoClass = SeckillRedisDao.class)
public class SeckillRedisDaoTest {

    private SeckillRedisDao seckillRedisDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Mock
    private DateProvider dateProvider;

    @PostConstruct
    void init() {
        seckillRedisDao = new SeckillRedisDao(stringRedisTemplate, redissonClient, dateProvider);
    }

    @BeforeEach
    void setup() {
        CommonTestHelper.clearRedis(stringRedisTemplate);
    }

    @Test
    void listSessionKeysActive() throws Exception {
        final String sessionKey1 = "seckill:sessions:220_280";
        final String sessionKey2 = "seckill:sessions:320_380";
        Mockito.when(dateProvider.nowInMillis()).thenReturn(260L);

        seckillRedisDao.saveSessionSkuKeys(sessionKey1, singletonList("1_2"));
        seckillRedisDao.saveSessionSkuKeys(sessionKey2, singletonList("3_4"));

        assertThat(seckillRedisDao.listSessionKeysActive()).isNotEmpty().containsExactly(sessionKey1);

    }

    @Test
    void listSessionSkus() throws Exception {
        final String sessionKey = "seckill:sessions:220_280";
        final String skuKey = "20_40";
        final long skuId = 40L;
        final SeckillSkuRedisTo skuTo = new SeckillSkuRedisTo().setPromotionSessionId(20L).setSkuId(skuId);

        seckillRedisDao.saveSessionSkuKeys(sessionKey, singletonList(skuKey));
        seckillRedisDao.saveSku(skuTo);

        assertThat(seckillRedisDao.listSessionSkus(sessionKey)).isNotEmpty().containsExactly(skuTo);
    }

    @Test
    void saveSessionSkuKeys_listSessionSkuKeys_hasSessionKey_listSessionKeysAll() throws Exception {
        final String sessionKey = "seckill:sessions:120_180";
        final String skuKey = "40_80";
        assertThat(seckillRedisDao.hasSessionKey(sessionKey)).isFalse();
        assertThat(seckillRedisDao.listSessionKeysAll()).isEmpty();
        assertThat(seckillRedisDao.listSessionSkuKeys(sessionKey)).isEmpty();

        seckillRedisDao.saveSessionSkuKeys(sessionKey, singletonList(skuKey));

        assertThat(seckillRedisDao.hasSessionKey(sessionKey)).isTrue();
        assertThat(seckillRedisDao.listSessionKeysAll()).isNotEmpty().containsExactly(sessionKey);
        assertThat(seckillRedisDao.listSessionSkuKeys(sessionKey)).isNotEmpty().containsExactly(skuKey);

    }

    @Test
    void sessionKeyToTimeRange() throws Exception {
        final Range<Long> result = seckillRedisDao.sessionKeyToTimeRange("seckill:sessions:120_180");
        assertThat(result).isNotNull();
        assertThat(result.toString()).isNotNull().isEqualTo(Range.between(120, 180).toString());
    }

    @Test
    void getSku_saveSku_hasSkuKe_listSkuKeys_getSkuBySkuId() throws Exception {
        final String skuKey = "30_50";
        final long skuId = 50L;
        final SeckillSkuRedisTo skuTo = new SeckillSkuRedisTo().setPromotionSessionId(30L).setSkuId(skuId);

        assertThat(seckillRedisDao.hasSkuKey(skuKey)).isFalse();
        assertThat(seckillRedisDao.listSkuKeys()).isEmpty();
        assertThat(seckillRedisDao.getSkuBySkuId(skuId)).isNull();

        seckillRedisDao.saveSku(skuTo);

        assertThat(seckillRedisDao.hasSkuKey(skuKey)).isTrue();
        assertThat(seckillRedisDao.listSkuKeys()).isNotEmpty().containsExactly(skuKey);

        assertThat(seckillRedisDao.getSkuBySkuId(skuId)).isNotNull().isEqualTo(skuTo);
        assertThat(seckillRedisDao.getSku(skuKey)).isNotNull().isEqualTo(skuTo);
    }

    @Test
    void setSkuStock_tryDeductStock() throws Exception {
        final String semaphoreKey = "seckill:stock:ggg";
        final long memberId = 5L;
        final int stockCount = 200;
        final String skuSecretCode = "ggg";
        final SeckillSkuRedisTo skuTo = new SeckillSkuRedisTo().setPromotionSessionId(70L).setSkuId(80L)
            .setRandomCode(skuSecretCode).setEndTime(Long.MAX_VALUE);
        Mockito.when(dateProvider.nowInMillis()).thenReturn(20L);

        assertThat(seckillRedisDao.tryDeductStock(memberId, skuTo, 10)).isFalse();
        seckillRedisDao.setSkuStock(skuSecretCode, stockCount);
        assertThat(redissonClient.getSemaphore(semaphoreKey).availablePermits()).isEqualTo(200);
        assertThat(seckillRedisDao.tryDeductStock(memberId, skuTo, 10)).isTrue();
        assertThat(redissonClient.getSemaphore(semaphoreKey).availablePermits()).isEqualTo(190);
        assertThat(seckillRedisDao.tryDeductStock(memberId, skuTo, 10)).isFalse();
        assertThat(redissonClient.getSemaphore(semaphoreKey).availablePermits()).isEqualTo(190);
    }
}
