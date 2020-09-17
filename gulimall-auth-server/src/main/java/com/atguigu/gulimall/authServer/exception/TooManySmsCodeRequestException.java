package com.atguigu.gulimall.authServer.exception;

public class TooManySmsCodeRequestException extends RuntimeException {
    public TooManySmsCodeRequestException() {
        super("验证码请求太频繁");
    }
}
