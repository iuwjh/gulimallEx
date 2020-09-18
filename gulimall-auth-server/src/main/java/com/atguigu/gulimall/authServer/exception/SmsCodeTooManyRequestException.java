package com.atguigu.gulimall.authServer.exception;

public class SmsCodeTooManyRequestException extends RuntimeException {
    public SmsCodeTooManyRequestException() {
        super("验证码请求太频繁");
    }
}
