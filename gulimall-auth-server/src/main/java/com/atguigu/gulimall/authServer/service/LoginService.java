package com.atguigu.gulimall.authServer.service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authServer.dao.SmsCodeRedisDao;
import com.atguigu.gulimall.authServer.exception.SmsCodeTooManyRequestException;
import com.atguigu.gulimall.authServer.feign.MemberFeignService;
import com.atguigu.gulimall.authServer.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.authServer.vo.SmsCodeVo;
import com.atguigu.gulimall.authServer.vo.UserLoginVo;
import com.atguigu.gulimall.authServer.vo.UserRegistVo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@AllArgsConstructor
public class LoginService {

    private final ThirdPartyFeignService thirdPartyFeignService;

    private final MemberFeignService memberFeignService;

    private final SmsCodeRedisDao smsCodeRedisDao;

    private static final Duration SMS_RESEND_INTERVAL = Duration.ofSeconds(60);

    private static final Duration SMS_VALID_FOR = Duration.ofMinutes(10);

    public void sendCode(String phone) {
        final SmsCodeVo smsCodeVo = smsCodeRedisDao.getExistCode(phone);
        if (smsCodeVo != null) {
            if (Instant.now().isBefore(smsCodeVo.getResendAfter())) {
                throw new SmsCodeTooManyRequestException();
            }
        }

        final SmsCodeVo newCode = smsCodeRedisDao.getNewCode(phone, SMS_RESEND_INTERVAL, SMS_VALID_FOR);

        thirdPartyFeignService.smsCodeToConsole(phone, newCode.getCode());
    }

    public boolean validateSmsCode(String phone, String code) {
        return smsCodeRedisDao.validateAndDelete(phone, code);
    }

    public R regist(UserRegistVo userRegistVo) {
        return memberFeignService.regist(userRegistVo);
    }

    public R login(UserLoginVo vo) {
        return memberFeignService.login(vo);
    }
}
