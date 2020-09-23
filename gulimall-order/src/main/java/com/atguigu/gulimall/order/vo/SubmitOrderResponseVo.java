package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.constant.OrderSubmitStatus;
import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private OrderSubmitStatus status;
}
