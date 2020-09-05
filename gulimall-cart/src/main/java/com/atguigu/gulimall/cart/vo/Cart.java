package com.atguigu.gulimall.cart.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> items;
    private BigDecimal reduce = BigDecimal.valueOf(0);

    @Setter(AccessLevel.NONE)
    private Integer countNum;
    @Setter(AccessLevel.NONE)
    private Integer countType;
    @Setter(AccessLevel.NONE)
    private BigDecimal totalAmount;

    public Integer getCountNum() {
        return items.stream().map(CartItem::getCount)
                .reduce(0, Integer::sum);
    }

    public Integer getCountType() {
        return items.size();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .filter(CartItem::getCheck)
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.valueOf(0), BigDecimal::add)
                .subtract(getReduce());
    }
}
