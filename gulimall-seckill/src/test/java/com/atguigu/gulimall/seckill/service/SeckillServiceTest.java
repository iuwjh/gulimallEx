package com.atguigu.gulimall.seckill.service;

import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.dao.amqp.SeckillRabbitSender;
import com.atguigu.gulimall.seckill.dao.redis.SeckillRedisDao;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.impl.SeckillServiceImpl;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SeckillServiceTest {

    @Mock
    private CouponFeignService couponFeignService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ProductFeignService productFeignService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private SeckillRabbitSender rabbitSender;

    @Mock
    private SeckillRedisDao seckillRedisDao;

    @Mock
    private DateProvider dateProvider;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        // seckillService = Mockito.spy(new SeckillServiceImpl());
    }

    // @Test
    void uploadSeckillSkuLatest3Days() throws Exception {
        final SeckillSessionsWithSkus sessionsWithSKus = new SeckillSessionsWithSkus();
        Mockito.when(couponFeignService.getLatest3DaySession()).thenReturn(R.ok().setData(singletonList(sessionsWithSKus)));

        seckillService.uploadSeckillSkuLatest3Days();
    }

    @Test
    void getCurrentSeckillSkus() throws Exception {
        final String sessionKey = "k1";
        final SeckillSkuRedisTo seckillSku = new SeckillSkuRedisTo().setSkuId(3L);
        Mockito.when(seckillRedisDao.listSessionKeysActive()).thenReturn(singleton(sessionKey));
        Mockito.when(seckillRedisDao.listSessionSkus(sessionKey)).thenReturn(singletonList(seckillSku));

        final List<SeckillSkuRedisTo> result = seckillService.getCurrentSeckillSkus();
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isNotNull().isEqualTo(seckillSku);
    }

    @Test
    void getSkuSeckillInfo() throws Exception {
        final long now = 30L;
        final long skuId = 3L;
        final SeckillSkuRedisTo seckillSku = new SeckillSkuRedisTo().setStartTime(40L).setEndTime(50L).setRandomCode("678");
        Mockito.when(seckillRedisDao.getSkuBySkuId(skuId)).thenReturn(seckillSku);
        Mockito.when(dateProvider.nowInMillis()).thenReturn(now);

        final SeckillSkuRedisTo result = seckillService.getSkuSeckillInfo(skuId);
        assertThat(result).isNotNull().isEqualTo(seckillSku.setRandomCode(null));
    }

    @Test
    void kill() throws Exception {
        final long memberId = 5L;
        ((ThreadLocal<MemberRespVo>) ReflectionTestUtils.getField(LoginUserInterceptor.class, "loginUserThreadLocal"))
            .set(new MemberRespVo().setId(memberId));

        final long now = 50L;
        final int skuCount = 5;
        final String skuKey = "k1";
        final String secretCode = "code1";
        final SeckillSkuRedisTo skuTo = new SeckillSkuRedisTo().setRandomCode(secretCode).setSeckillLimit(10)
            .setPromotionSessionId(8L).setSkuId(9L).setSeckillPrice(BigDecimal.valueOf(300))
            .setStartTime(40L).setEndTime(60L);
        final SeckillOrderTo orderTo = objectMapper.convertValue(skuTo, SeckillOrderTo.class).setMemberId(memberId).setNum(skuCount);
        Mockito.when(seckillRedisDao.getSku(skuKey)).thenReturn(skuTo);
        Mockito.when(seckillRedisDao.tryDeductStock(memberId, skuTo, skuCount)).thenReturn(true);
        Mockito.when(dateProvider.nowInMillis()).thenReturn(now);

        final String result = seckillService.kill(skuKey, secretCode, skuCount);
        assertThat(result).isNotBlank();
        Mockito.verify(rabbitSender, Mockito.times(1)).createOrder(orderTo.setOrderSn(result));
    }
}
