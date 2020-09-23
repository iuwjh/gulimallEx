package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class WareSkuLockVo {
    String orderSn;
    List<OrderItemVo> itemsToLock;
}
