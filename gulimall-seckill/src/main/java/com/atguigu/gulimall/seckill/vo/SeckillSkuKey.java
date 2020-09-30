package com.atguigu.gulimall.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeckillSkuKey {
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    public String getKey() {
        return promotionSessionId + "_" + skuId;
    }

    public static SeckillSkuKey fromString(String key) {
        final String[] split = key.split("_");
        return new SeckillSkuKey(Long.valueOf(split[0]), Long.valueOf(split[1]));
    }
}
