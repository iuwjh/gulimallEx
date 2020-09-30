package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.dao.amqp.SeckillRabbitSender;
import com.atguigu.gulimall.seckill.dao.redis.SeckillRedisDao;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final CouponFeignService couponFeignService;

    private final ProductFeignService productFeignService;

    private final RedissonClient redissonClient;

    private final SeckillRabbitSender rabbitSender;

    private final SeckillRedisDao seckillRedisDao;

    private final DateProvider dateProvider;

    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    public static final String SKUKILL_CACHE_PREFIX = "seckill:skus";

    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void uploadSeckillSkuLatest3Days() {
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkus> sessions = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});

            if (sessions != null) {
                saveSessionInfos(sessions);
                saveSessionSkuInfos(sessions);
            }
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // long time = new Date().getTime();
        //
        // Set<String> sessionKeys = seckillRedisDao.listSessionKeysAll();
        // for (String sessionKey : sessionKeys) {
        //
        //     String[] split = sessionKey.replace(SESSIONS_CACHE_PREFIX, "").split("_");
        //     long start = Long.parseLong(split[0]);
        //     long end = Long.parseLong(split[1]);
        //     if (time >= start && time <= end) {
        //         BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //         List<String> range = stringRedisTemplate.opsForList().range(sessionKey, -100, 100);
        //         if (range != null && !range.isEmpty()) {
        //             List<String> strings = hashOps.multiGet(range);
        //             if (strings != null && !strings.isEmpty()) {
        //                 return strings.stream()
        //                         .map((item) -> JSON.parseObject(item, SeckillSkuRedisTo.class))
        //                         .collect(Collectors.toList());
        //             }
        //         }
        //         return null;
        //     }
        // }
        // return null;

        return seckillRedisDao.listSessionKeysActive().stream()
            .flatMap(activeSessionKey -> seckillRedisDao.listSessionSkus(activeSessionKey).stream())
            .collect(Collectors.toList());

    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //
        // Set<String> skuKeys = hashOps.keys();
        // if (skuKeys != null && skuKeys.size() > 0) {
        //     Pattern pattern = Pattern.compile("\\d_" + skuId);
        //     for (String skuKey : skuKeys) {
        //         if (pattern.matcher(skuKey).matches()) {
        //             String json = hashOps.get(skuKey);
        //             SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
        //
        //             long currrntTime = new Date().getTime();
        //             if (currrntTime >= skuRedisTo.getStartTime() && currrntTime <= skuRedisTo.getEndTime()) {
        //
        //             } else {
        //                 skuRedisTo.setRandomCode(null);
        //             }
        //             return skuRedisTo;
        //         }
        //     }
        // }
        // return null;

        final SeckillSkuRedisTo seckillSku = seckillRedisDao.getSkuBySkuId(skuId);
        if (seckillSku != null) {
            if (!isSessionActive(seckillSku)) {
                seckillSku.setRandomCode(null);
            }
        }
        return seckillSku;
    }

    @Override
    public String kill(String seckillSkuKey, String secretCode, Integer skuCount) {

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();

        // BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //
        // String json = hashOps.get(seckillSkuKey);
        // if (!StringUtils.isEmpty(json)) {
        //     SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
        //
        //     long timeNow = new Date().getTime();
        //     if (timeNow >= redisTo.getStartTime() && timeNow <= redisTo.getEndTime()) {
        //
        //         String skuSessionId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
        //         if (redisTo.getRandomCode().equals(secretCode)
        //             && seckillSkuKey.equals(skuSessionId)
        //             && skuCount <= redisTo.getSeckillLimit()) {
        //
        //             String redisKey = memberRespVo.getId() + "_" + skuSessionId;
        //             Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, skuCount.toString(),
        //                 redisTo.getEndTime() - timeNow, TimeUnit.MILLISECONDS);
        //             if (b) {
        //                 RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
        //                 boolean b1 = semaphore.tryAcquire(skuCount);
        //                 if (b1) {
        //                     String orderSn = IdWorker.getTimeId();
        //                     SeckillOrderTo orderTo = new SeckillOrderTo();
        //                     orderTo.setOrderSn(orderSn);
        //                     orderTo.setMemberId(memberRespVo.getId());
        //                     orderTo.setNum(skuCount);
        //                     orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
        //                     orderTo.setSkuId(redisTo.getSkuId());
        //                     orderTo.setSeckillPrice(redisTo.getSeckillPrice());
        //
        //                     rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
        //
        //                     return orderSn;
        //                 }
        //             }
        //         }
        //     }
        // }
        //
        // return null;

        final SeckillSkuRedisTo seckillSku = seckillRedisDao.getSku(seckillSkuKey);
        if (seckillSku != null) {

            if (isSessionActive(seckillSku)
                && seckillSku.getRandomCode().equals(secretCode)
                && seckillSku.getSeckillLimit() > skuCount) {

                if (seckillRedisDao.tryDeductStock(memberRespVo.getId(), seckillSku, skuCount)) {

                    String orderSn = IdWorker.getTimeId();
                    SeckillOrderTo orderTo = new SeckillOrderTo()
                        .setOrderSn(orderSn)
                        .setMemberId(memberRespVo.getId())
                        .setNum(skuCount)
                        .setPromotionSessionId(seckillSku.getPromotionSessionId())
                        .setSkuId(seckillSku.getSkuId())
                        .setSeckillPrice(seckillSku.getSeckillPrice());

                    rabbitSender.createOrder(orderTo);

                    return orderSn;
                }
            }
        }
        return null;
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        for (SeckillSessionsWithSkus session : sessions) {
            // BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            for (SeckillSkuVo skuVo : session.getRelationSkus()) {
                String skuKey = toSkuKey(skuVo.getPromotionSessionId(), skuVo.getSkuId());

                final Boolean skuKeyExist = seckillRedisDao.hasSkuKey(skuKey);
                if (skuKeyExist != null && !skuKeyExist) {
                    SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo();

                    R r = productFeignService.getSkuInfo(skuVo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        skuRedisTo.setSkuInfo(skuInfo);
                    }

                    BeanUtils.copyProperties(skuVo, skuRedisTo);

                    skuRedisTo.setStartTime(session.getStartTime().getTime());
                    skuRedisTo.setEndTime(session.getEndTime().getTime());

                    String token = RandomStringUtils.randomAlphanumeric(16);
                    skuRedisTo.setRandomCode(token);
                    // String s = JSON.toJSONString(skuRedisTo);

                    System.out.println("秒杀：商品上架 " + skuKey + " " + skuRedisTo);
                    // hashOps.put(skuKey, s);
                    seckillRedisDao.saveSku(skuRedisTo);

                    String semaphoreKey = SKU_STOCK_SEMAPHORE + token;
                    RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
                    semaphore.trySetPermits(skuVo.getSeckillCount());
                }

            }

        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        for (SeckillSessionsWithSkus session : sessions) {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            final String sessionKey = toSessionKey(startTime, endTime);
            final Boolean sessionKeyExist = seckillRedisDao.hasSessionKey(sessionKey);
            if (sessionKeyExist != null && !sessionKeyExist) {
                List<String> skuKeys = session.getRelationSkus().stream()
                    .map(seckillSku -> toSkuKey(seckillSku.getPromotionSessionId(), seckillSku.getSkuId()))
                    .collect(Collectors.toList());

                System.out.println("秒杀：新活动 " + sessionKey + " " + skuKeys);
                seckillRedisDao.saveSessionSkuKeys(sessionKey, skuKeys);
            }
        }
    }

    private boolean isSessionActive(SeckillSkuRedisTo seckillSkuRedisTo) {
        final long now = dateProvider.nowInMillis();
        return seckillSkuRedisTo.getStartTime() <= now && now <= seckillSkuRedisTo.getEndTime();
    }

    private String toSessionKey(long startTime, long endTime) {
        return SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
    }

    private String toSkuKey(long promotionSessionId, long skuId) {
        return promotionSessionId + "_" + skuId;
    }
}
