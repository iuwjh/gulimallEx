package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    OrderEntity order;
    List<OrderItemEntity> orderItems;
    BigDecimal payPrice;
    BigDecimal fare;
}