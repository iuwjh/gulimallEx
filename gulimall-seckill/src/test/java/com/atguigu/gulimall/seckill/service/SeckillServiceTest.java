package com.atguigu.gulimall.seckill.service;

import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RandomProvider;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.dao.amqp.SeckillRabbitSender;
import com.atguigu.gulimall.seckill.dao.redis.SeckillRedisDao;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.impl.SeckillServiceImpl;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Date;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SeckillServiceTest {

    @Mock
    private CouponFeignService couponFeignService;

    @Mock
    private ProductFeignService productFeignService;

    @Mock
    private SeckillRabbitSender rabbitSender;

    @Mock
    private SeckillRedisDao seckillRedisDao;

    @Mock
    private DateProvider dateProvider;

    @Mock
    private RandomProvider randomProvider;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        // seckillService = Mockito.spy(new SeckillServiceImpl());
    }

    @Test
    void uploadSeckillSkuLatest3Days() throws Exception {
        final String secretCode = "aaabbbccceeeffff";
        final SeckillSkuVo skuVo = new SeckillSkuVo().setPromotionSessionId(30L).setSkuId(40L).setSeckillCount(200);
        final SeckillSessionsWithSkus sessionsWithSKus = new SeckillSessionsWithSkus().setStartTime(new Date(40L))
            .setEndTime(new Date(60L)).setRelationSkus(singletonList(skuVo));
        final SkuInfoVo skuInfoVo = new SkuInfoVo().setSpuId(7L);
        final SeckillSkuRedisTo skuRedisTo = objectMapper.convertValue(skuVo, SeckillSkuRedisTo.class)
            .setStartTime(40L).setEndTime(60L).setRandomCode(secretCode);
        Mockito.when(couponFeignService.getLatest3DaySession()).thenReturn(R.ok().setData(singletonList(sessionsWithSKus)));
        Mockito.when(productFeignService.getSkuInfo(skuVo.getSkuId())).thenReturn(R.ok().setData(skuInfoVo));
        Mockito.when(randomProvider.alphanumeric(16)).thenReturn(secretCode);

        seckillService.uploadSeckillSkuLatest3Days();
        Mockito.verify(seckillRedisDao, Mockito.times(1))
            .saveSessionSkuKeys(SeckillRedisDao.SESSIONS_CACHE_PREFIX + "40_60",
                singletonList("30_40"));
        Mockito.verify(seckillRedisDao, Mockito.times(1)).saveSku(skuRedisTo);
        Mockito.verify(seckillRedisDao, Mockito.times(1))
            .setSkuStock(secretCode, skuVo.getSeckillCount());
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

        final String result = seckillService.tryOrder(skuKey, secretCode, skuCount);
        assertThat(result).isNotBlank();
        Mockito.verify(rabbitSender, Mockito.times(1)).createOrder(orderTo.setOrderSn(result));
    }
}
