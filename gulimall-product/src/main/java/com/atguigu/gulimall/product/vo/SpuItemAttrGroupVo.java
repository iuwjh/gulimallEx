package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
