package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;

    private String orderToken;
    private BigDecimal payPrice;
    private String note;
}
