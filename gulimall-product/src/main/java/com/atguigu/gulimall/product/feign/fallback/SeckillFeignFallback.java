package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

@Component
public class SeckillFeignFallback implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        System.out.println("熔断回退");
        return R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMsg());
    }
}
