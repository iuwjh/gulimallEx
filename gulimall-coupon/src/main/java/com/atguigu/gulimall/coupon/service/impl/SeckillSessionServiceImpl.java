package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
@RequiredArgsConstructor
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    private final SeckillSkuRelationDao seckillSkuRelationDao;

    private final SeckillSessionDao seckillSessionDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
            new Query<SeckillSessionEntity>().getPage(params),
            new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(formatter);
        String endTime = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX).format(formatter);
        List<SeckillSessionEntity> list = seckillSessionDao.listBetween(startTime, endTime);
        if (list != null && list.size() > 0) {
            return list.stream().peek((session) -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationDao.listBySessionId(session.getId());
                session.setRelationSkus(relationEntities);
            }).collect(Collectors.toList());
        }
        return null;
    }

}
