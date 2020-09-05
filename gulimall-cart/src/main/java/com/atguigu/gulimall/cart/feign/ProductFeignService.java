package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.vo.SkuSaleAttrValueTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("product/skuinfo/info/{skuId}")
    R skuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("product/skusaleattrvalue/infoBySkuId/{id}")
    List<SkuSaleAttrValueTo> infoBySkuId(@PathVariable("id") Long skuId);

    @GetMapping("product/skuinfo/{skuId}/price")
    R getPrice(@PathVariable Long skuId);
}
