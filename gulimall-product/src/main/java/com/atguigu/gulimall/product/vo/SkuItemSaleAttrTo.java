package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class SkuItemSaleAttrTo {
    private Long attrId;
    private String attrName;
    private String attrValue;
    private String skuIds;
}
