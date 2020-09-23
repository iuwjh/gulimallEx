package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.constant.OrderConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

@Repository
@RequiredArgsConstructor
public class OrderRedisDao {

    private final StringRedisTemplate stringRedisTemplate;

    public void setOrderToken(long memberId, String orderToken, Duration validFor) {
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberId, orderToken, validFor);
    }

    public boolean removeOrderTokenIfExist(String key, String orderToken) {
        final String delScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        final Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(delScript, Long.class), singletonList(key), orderToken);
        return result != null && result != 0;
    }
}
