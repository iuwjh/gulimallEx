package com.atguigu.gulimall.seckill.dao.redis;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.DateProvider;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Range;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeckillRedisDao {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final DateProvider dateProvider;

    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    public static final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    private BoundHashOperations<String, String, String> skuHashOps;

    @PostConstruct
    void init() {
        skuHashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
    }

    public Boolean hasSessionKey(String sessionKey) {
        return stringRedisTemplate.hasKey(sessionKey);
    }

    public Set<String> listSessionKeysAll() {
        return stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
    }

    public Set<String> listSessionKeysActive() {
        final Set<String> sessionKeys = listSessionKeysAll();
        if (sessionKeys != null) {
            return sessionKeys.stream()
                .filter((sessionKey) -> sessionKeyToTimeRange(sessionKey).contains(dateProvider.nowInMillis()))
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public List<String> listSessionSkuKeys(String sessionKey) {
        return stringRedisTemplate.opsForList().range(sessionKey, -100, 100);
    }

    public List<SeckillSkuRedisTo> listSessionSkus(String sessionKey) {
        return Optional.ofNullable(sessionKey)
            .map(this::listSessionSkuKeys)
            .map(skuHashOps::multiGet)
            .map(skus -> skus.stream().map(sku -> JSON.parseObject(sku, SeckillSkuRedisTo.class)).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    public void saveSessionSkuKeys(String sessionKey, List<String> skuKeys) {
        stringRedisTemplate.opsForList().leftPushAll(sessionKey, skuKeys);
    }

    public Range<Long> sessionKeyToTimeRange(String sessionKey) {
        String[] split = sessionKey.replace(SESSIONS_CACHE_PREFIX, "").split("_");
        return Range.between(Long.parseLong(split[0]), Long.parseLong(split[1]));
    }

    public SeckillSkuRedisTo getSkuBySkuId(long skuId) {
        try (Cursor<Map.Entry<String, String>> skuCursor = skuHashOps.scan(ScanOptions.scanOptions().match("*_" + skuId).build())) {
            if (skuCursor.hasNext()) {
                final Map.Entry<String, String> skuEntry = skuCursor.next();
                return JSON.parseObject(skuEntry.getValue(), SeckillSkuRedisTo.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SeckillSkuRedisTo getSku(String seckillSkuKey) {
        return Optional.ofNullable(seckillSkuKey)
            .map(skuHashOps::get)
            .map(json -> JSON.parseObject(json, SeckillSkuRedisTo.class))
            .orElse(null);
    }

    public void saveSku(SeckillSkuRedisTo skuTo) {
        final String skuKey = skuTo.getPromotionSessionId() + "_" + skuTo.getSkuId();
        skuHashOps.put(skuKey, JSON.toJSONString(skuTo));
    }

    public Boolean hasSkuKey(String skuKey) {
        return skuHashOps.hasKey(skuKey);
    }

    // TODO 同步
    public boolean tryDeductStock(Long memberId, SeckillSkuRedisTo skuTo, Integer skuCount) {
        final long now = dateProvider.nowInMillis();
        final String memberOrderKey = memberId + "_" + skuTo.getPromotionSessionId() + "_" + skuTo.getSkuId();
        Boolean isFirstTimeOrder = stringRedisTemplate.opsForValue().setIfAbsent(memberOrderKey, skuCount.toString(),
            skuTo.getEndTime() - now, TimeUnit.MILLISECONDS);
        if (isFirstTimeOrder != null && isFirstTimeOrder) {
            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + skuTo.getRandomCode());
            boolean isStockDeducted = semaphore.tryAcquire(skuCount);
            return isStockDeducted;
        }
        return false;
    }

}
