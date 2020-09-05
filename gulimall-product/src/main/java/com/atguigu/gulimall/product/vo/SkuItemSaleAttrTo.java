package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.Map;

@Data
public class SkuItemSaleAttrTo {
    private Long attrId;
    private String attrName;
    private String attrValue;
    private String skuIds;
}
