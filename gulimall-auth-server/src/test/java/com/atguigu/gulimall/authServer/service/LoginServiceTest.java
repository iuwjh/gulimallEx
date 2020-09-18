package com.atguigu.gulimall.authServer.service;

import com.atguigu.gulimall.authServer.dao.SmsCodeRedisDao;
import com.atguigu.gulimall.authServer.exception.SmsCodeTooManyRequestException;
import com.atguigu.gulimall.authServer.feign.MemberFeignService;
import com.atguigu.gulimall.authServer.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.authServer.vo.SmsCodeVo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {
    @Mock
    private ThirdPartyFeignService thirdPartyFeignService;

    @Mock
    private SmsCodeRedisDao smsCodeRedisDao;

    @Mock
    private MemberFeignService memberFeignService;

    private LoginService loginService;

    @BeforeEach
    void setup() {
        loginService = new LoginService(thirdPartyFeignService, memberFeignService, smsCodeRedisDao);
    }

    @Test
    void sendCodeTooManyRequest() throws Exception {
        final SmsCodeVo smsCodeVo = createSmsCodeVo().setResendAfter(Instant.now().plus(Duration.ofDays(1)));
        Mockito.when(smsCodeRedisDao.getExistCode(anyString())).thenReturn(smsCodeVo);

        Assertions.assertThatThrownBy(() -> {
            loginService.sendCode("");
        }).isInstanceOf(SmsCodeTooManyRequestException.class);
    }

    @Test
    void sendCodeSucceed() throws Exception {
        final SmsCodeVo smsCodeVo = createSmsCodeVo();
        final String phone = "18000000000";
        Mockito.when(smsCodeRedisDao.getExistCode(anyString())).thenReturn(null);
        Mockito.when(smsCodeRedisDao.getNewCode(anyString(), any(), any())).thenReturn(smsCodeVo);

        loginService.sendCode(phone);
        Mockito.verify(thirdPartyFeignService, Mockito.times(1)).smsCodeToConsole(phone, smsCodeVo.getCode());
    }

    private SmsCodeVo createSmsCodeVo() {
        return new SmsCodeVo().setCode("333333").setResendAfter(Instant.now());

    }
}
