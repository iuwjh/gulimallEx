package com.atguigu.gulimall.cart.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class CartItem {
    private Long skuId;
    private Boolean check = true;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;

    @Setter(AccessLevel.NONE)
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        if (this.price != null && this.count != null) {
            return this.price.multiply(BigDecimal.valueOf(this.count));
        }
        return null;
    }

}
