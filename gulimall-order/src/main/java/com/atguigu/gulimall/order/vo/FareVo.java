package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
