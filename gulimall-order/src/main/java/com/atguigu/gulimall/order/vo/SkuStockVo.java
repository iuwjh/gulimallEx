package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkuStockVo {
    private Long skuId;
    private Boolean hasStock;
}
