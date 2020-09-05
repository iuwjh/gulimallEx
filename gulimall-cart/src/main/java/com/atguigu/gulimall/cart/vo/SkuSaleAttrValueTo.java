package com.atguigu.gulimall.cart.vo;

import lombok.Data;

@Data
public class SkuSaleAttrValueTo {
    /**
     * attr_id
     */
    private Long attrId;
    /**
     * 销售属性名
     */
    private String attrName;
    /**
     * 销售属性值
     */
    private String attrValue;
    /**
     * 顺序
     */
    private Integer attrSort;
}
