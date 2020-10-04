package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    /**
     * 上传最近3天的秒杀活动信息
     */
    void uploadSeckillSkuLatest3Days();

    /**
     * 获取当前正在秒杀的商品信息
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 获取商品的秒杀信息
     * @param skuId
     * @return 如果商品没有参与秒杀，返回null
     */
    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    /**
     * 尝试秒杀下订单
     * @param seckillSkuKey
     * @param secretCode
     * @param skuCount
     * @return 如果秒杀失败，返回null
     */
    String tryOrder(String seckillSkuKey, String secretCode, Integer skuCount);
}
