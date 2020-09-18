package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserInfoTo {
    private Long userId;
    private String userKey;

    private boolean tempUser = false;
}
