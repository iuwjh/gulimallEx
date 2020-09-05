package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValuesVO> attrValues;

    @Data
    public static class AttrValuesVO {
        private String val;
        private boolean checked;
        private String skuIds;
    }
}
