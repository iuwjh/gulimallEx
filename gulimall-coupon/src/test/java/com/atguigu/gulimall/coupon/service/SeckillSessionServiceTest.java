package com.atguigu.gulimall.coupon.service;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.impl.SeckillSessionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SeckillSessionServiceTest {

    @Mock
    private SeckillSkuRelationDao seckillSkuRelationDao;

    @Mock
    private SeckillSessionDao seckillSessionDao;

    private SeckillSessionService seckillSessionService;

    @BeforeEach
    void setup() {
        seckillSessionService = Mockito.spy(new SeckillSessionServiceImpl(seckillSkuRelationDao, seckillSessionDao));
    }

    @Test
    void getLatest3DaySession() throws Exception {
        final long seckillSessionId = 5L;
        final long seckillSkuRelationId = 6L;
        Mockito.when(seckillSessionDao.listBetween(any(), any())).thenReturn(singletonList(new SeckillSessionEntity().setId(seckillSessionId)));
        Mockito.when(seckillSkuRelationDao.listBySessionId(seckillSessionId)).thenReturn(singletonList(new SeckillSkuRelationEntity().setId(seckillSkuRelationId)));

        final List<SeckillSessionEntity> result = seckillSessionService.getLatest3DaySession();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(seckillSessionId);
        assertThat(result.get(0).getRelationSkus()).hasSize(1);
        assertThat(result.get(0).getRelationSkus().get(0).getId()).isEqualTo(seckillSkuRelationId);
    }
}
