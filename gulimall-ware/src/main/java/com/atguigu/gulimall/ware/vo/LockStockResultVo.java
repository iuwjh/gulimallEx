package com.atguigu.gulimall.ware.vo;

import lombok.Data;

@Data
public class LockStockResultVo {
    Long skuId;
    Integer num;
    Boolean locked;
}
