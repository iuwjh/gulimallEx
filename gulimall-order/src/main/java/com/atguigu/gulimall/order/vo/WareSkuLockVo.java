package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    String orderSn;
    List<OrderItemVo> locks;
}
