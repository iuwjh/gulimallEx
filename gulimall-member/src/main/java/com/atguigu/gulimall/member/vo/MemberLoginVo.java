package com.atguigu.gulimall.member.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MemberLoginVo {
    private String loginAcct;
    private String password;
}
