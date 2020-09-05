package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {
    private String orderSn;

    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer num;

    private Long memberId;

}
