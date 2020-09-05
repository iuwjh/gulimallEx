package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @RequestMapping("coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();
}
