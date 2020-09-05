package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSKus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;


    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    public static final String SKUKILL_CACHE_PREFIX = "seckill:skus";

    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void uploadSeckillSkuLatest3Days() {
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSKus> sessions = r.getData(new TypeReference<List<SeckillSessionsWithSKus>>() {
            });

            if (sessions != null) {
                saveSessionInfos(sessions);
                saveSessionSkuInfos(sessions);
            }
        }


    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        long time = new Date().getTime();

        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {

            String[] split = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
            long start = Long.parseLong(split[0]);
            long end = Long.parseLong(split[1]);
            if (time >= start && time <= end) {
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                if (range != null && !range.isEmpty()) {
                    List<String> strings = hashOps.multiGet(range);
                    if (strings != null && !strings.isEmpty()) {
                        return strings.stream()
                                .map((item) -> JSON.parseObject(item, SeckillSkuRedisTo.class))
                                .collect(Collectors.toList());
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            Pattern pattern = Pattern.compile("\\d_" + skuId);
            for (String key : keys) {
                if (pattern.matcher(key).matches()) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    long currrntTime = new Date().getTime();
                    if (currrntTime >= skuRedisTo.getStartTime() && currrntTime <= skuRedisTo.getEndTime()) {

                    } else {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();

        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        String json = hashOps.get(killId);
        if (!StringUtils.isEmpty(json)) {
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

            long timeNow = new Date().getTime();
            if (timeNow >= redisTo.getStartTime() && timeNow <= redisTo.getEndTime()) {

                String skuSessionId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (redisTo.getRandomCode().equals(key)
                        && killId.equals(skuSessionId)
                        && num <= redisTo.getSeckillLimit()) {

                    String redisKey = memberRespVo.getId() + "_" + skuSessionId;
                    Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(),
                            redisTo.getEndTime() - timeNow, TimeUnit.MILLISECONDS);
                    if (b) {
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
                        boolean b1 = semaphore.tryAcquire(num);
                        if (b1) {
                            String orderSn = IdWorker.getTimeId();
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setOrderSn(orderSn);
                            orderTo.setMemberId(memberRespVo.getId());
                            orderTo.setNum(num);
                            orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                            orderTo.setSkuId(redisTo.getSkuId());
                            orderTo.setSeckillPrice(redisTo.getSeckillPrice());

                            rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);

                            return orderSn;
                        }
                    }
                }
            }
        }

        return null;
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSKus> sessions) {
        for (SeckillSessionsWithSKus session : sessions) {
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            for (SeckillSkuVo seckillSkuVo : session.getRelationSkus()) {
                String hashKey = seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString();

                if (!hashOps.hasKey(hashKey)) {
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                    R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(skuInfo);
                    }

                    BeanUtils.copyProperties(seckillSkuVo, redisTo);

                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    String token = RandomStringUtils.randomAlphanumeric(16);
                    redisTo.setRandomCode(token);
                    String s = JSON.toJSONString(redisTo);

                    System.out.println("秒杀：商品上架 " + hashKey + " " + s);
                    hashOps.put(hashKey, s);

                    String semaphoreKey = SKU_STOCK_SEMAPHORE + token;
                    RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }

            }

        }

    }

    private void saveSessionInfos(List<SeckillSessionsWithSKus> sessions) {
        for (SeckillSessionsWithSKus session : sessions) {
            long startTime = session.getStartTime().getTime();
            long endtime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endtime;
            if (!stringRedisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()).collect(Collectors.toList());

                System.out.println("秒杀：新活动 " + key + " " + collect);
                stringRedisTemplate.opsForList().leftPushAll(key, collect);
            }
        }
    }
}
