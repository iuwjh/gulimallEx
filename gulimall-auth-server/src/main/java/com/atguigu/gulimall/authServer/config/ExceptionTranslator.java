package com.atguigu.gulimall.authServer.config;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.authServer.controller.LoginController;
import com.atguigu.gulimall.authServer.exception.SmsCodeTooManyRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpSession;

@Slf4j
@RestControllerAdvice(basePackageClasses = LoginController.class)
public class ExceptionTranslator {

    @ExceptionHandler(value = SmsCodeTooManyRequestException.class)
    public R tooManySmsCodeRequestExceptionHandler(SmsCodeTooManyRequestException e, HttpSession session) {
        MemberRespVo memberVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        log.error("SMS请求太频繁{}，异常类型：{}，来自：{}", e.getMessage(), e.getClass(), memberVo.getUsername());
        return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
    }
}
