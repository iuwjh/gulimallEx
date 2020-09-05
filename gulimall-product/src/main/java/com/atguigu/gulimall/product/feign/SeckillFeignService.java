package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SeckillFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "gulimall-seckill", fallback = SeckillFeignFallback.class)
public interface SeckillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable Long skuId);
}
