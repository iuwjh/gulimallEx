package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// @Service
public class SecKillScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    public static final String upload_lock = "seckill:upload:lock";

    @Scheduled(cron = "*/15 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // System.out.println("定时：上架秒杀商品");

        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);

        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

    }
}
