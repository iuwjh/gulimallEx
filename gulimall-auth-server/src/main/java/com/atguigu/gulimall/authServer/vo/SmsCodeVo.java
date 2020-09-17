package com.atguigu.gulimall.authServer.vo;

import lombok.Data;

import java.time.Instant;

@Data
public class SmsCodeVo {
    String code;
    Instant validBefore;
}
