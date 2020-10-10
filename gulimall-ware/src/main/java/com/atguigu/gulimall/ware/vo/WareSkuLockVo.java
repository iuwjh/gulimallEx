package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    String orderSn;
    List<OrderItemVo> itemsToLock;
}
