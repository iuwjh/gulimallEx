package com.atguigu.gulimall.order.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class OrderConfirmVo {
    List<MemberAddressVo> address;

    List<OrderItemVo> items;

    String orderToken;

    Integer integration;

    Map<Long,Boolean> stocks;

    @Setter(AccessLevel.NONE)
    BigDecimal total;
    @Setter(AccessLevel.NONE)
    BigDecimal payPrice;
    @Setter(AccessLevel.NONE)
    Integer count;

    public Integer getCount() {
        if (items != null) {
            return items.stream()
                    .map(OrderItemVo::getCount)
                    .reduce(0, Integer::sum);
        }
        return null;
    }

    public BigDecimal getTotal() {
        if (items != null) {
            return items.stream()
                    .map((item) -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                    .reduce(BigDecimal.valueOf(0), BigDecimal::add);
        }
        return total;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
