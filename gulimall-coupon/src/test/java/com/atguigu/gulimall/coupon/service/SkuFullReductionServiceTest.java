package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.service.impl.SkuFullReductionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SkuFullReductionServiceTest {

    @Mock
    private SkuLadderService skuLadderService;

    @Mock
    private MemberPriceService memberPriceService;

    @Mock
    private SkuFullReductionDao skuFullReductionDao;

    private SkuFullReductionService skuFullReductionService;

    @BeforeEach
    void setup() {
        skuFullReductionService = Mockito.spy(new SkuFullReductionServiceImpl(skuLadderService, memberPriceService, skuFullReductionDao));
    }

    @Test
    void saveSkuReduction() throws Exception {
        final SkuReductionTo reductionTo = new SkuReductionTo().setFullCount(3).setFullPrice(BigDecimal.valueOf(200))
            .setMemberPrice(singletonList(new MemberPrice().setPrice(BigDecimal.valueOf(500))));
        skuFullReductionService.saveSkuReduction(reductionTo);

        Mockito.verify(skuLadderService, Mockito.times(1)).save(any());
        Mockito.verify(skuFullReductionDao, Mockito.times(1)).insert(any());
        Mockito.verify(memberPriceService, Mockito.times(1)).saveBatch(any());
    }
}
