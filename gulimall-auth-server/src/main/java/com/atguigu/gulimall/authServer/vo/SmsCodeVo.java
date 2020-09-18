package com.atguigu.gulimall.authServer.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class SmsCodeVo {
    String code;
    Instant resendAfter;
}
