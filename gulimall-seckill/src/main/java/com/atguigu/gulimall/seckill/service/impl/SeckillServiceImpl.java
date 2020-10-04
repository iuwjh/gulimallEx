package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.TypeReference;
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
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final CouponFeignService couponFeignService;

    private final ProductFeignService productFeignService;

    private final SeckillRabbitSender rabbitSender;

    private final SeckillRedisDao seckillRedisDao;

    private final DateProvider dateProvider;

    private final RandomProvider randomProvider;

    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        R latest3DaySessionResponse = couponFeignService.getLatest3DaySession();
        if (latest3DaySessionResponse.getCode() == 0) {
            List<SeckillSessionsWithSkus> sessions = latest3DaySessionResponse.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});

            if (sessions != null) {
                for (SeckillSessionsWithSkus session : sessions) {
                    // 保存场次信息
                    final String sessionKey = toSessionKey(session.getStartTime().getTime(), session.getEndTime().getTime());
                    final Boolean sessionKeyExist = seckillRedisDao.hasSessionKey(sessionKey);
                    if (sessionKeyExist != null && !sessionKeyExist) {
                        List<String> skuKeys = session.getRelationSkus().stream()
                            .map(seckillSku -> toSkuKey(seckillSku.getPromotionSessionId(), seckillSku.getSkuId()))
                            .collect(Collectors.toList());

                        System.out.println("秒杀：新活动 " + sessionKey + " " + skuKeys);
                        seckillRedisDao.saveSessionSkuKeys(sessionKey, skuKeys);
                    }

                    // 保存场次的商品信息
                    for (SeckillSkuVo skuVo : session.getRelationSkus()) {
                        String skuKey = toSkuKey(skuVo.getPromotionSessionId(), skuVo.getSkuId());
                        final Boolean skuKeyExist = seckillRedisDao.hasSkuKey(skuKey);
                        if (skuKeyExist != null && !skuKeyExist) {
                            SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo();

                            R r = productFeignService.getSkuInfo(skuVo.getSkuId());
                            if (r.getCode() == 0) {
                                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                                skuRedisTo.setSkuInfo(skuInfo);
                            }

                            BeanUtils.copyProperties(skuVo, skuRedisTo);

                            skuRedisTo.setStartTime(session.getStartTime().getTime());
                            skuRedisTo.setEndTime(session.getEndTime().getTime());

                            String secretCode = randomProvider.alphanumeric(16);
                            skuRedisTo.setRandomCode(secretCode);

                            System.out.println("秒杀：商品上架 " + skuKey + " " + skuRedisTo);
                            seckillRedisDao.saveSku(skuRedisTo);

                            seckillRedisDao.setSkuStock(secretCode, skuVo.getSeckillCount());
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        return seckillRedisDao.listSessionKeysActive().stream()
            .flatMap(activeSessionKey -> seckillRedisDao.listSessionSkus(activeSessionKey).stream())
            .collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

        final SeckillSkuRedisTo seckillSku = seckillRedisDao.getSkuBySkuId(skuId);
        if (seckillSku != null) {
            if (!isSessionActive(seckillSku)) {
                seckillSku.setRandomCode(null);
            }
        }
        return seckillSku;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String tryOrder(String seckillSkuKey, String secretCode, Integer skuCount) {

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();

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
