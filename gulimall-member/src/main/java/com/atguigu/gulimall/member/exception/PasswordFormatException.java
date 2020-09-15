package com.atguigu.gulimall.member.exception;

public class PasswordFormatException extends RuntimeException {
    public PasswordFormatException() {
        super("密码格式错误");
    }
}
