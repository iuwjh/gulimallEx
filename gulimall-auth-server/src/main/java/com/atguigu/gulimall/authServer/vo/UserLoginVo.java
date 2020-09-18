package com.atguigu.gulimall.authServer.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserLoginVo {
    private String loginAcct;
    private String password;
}
