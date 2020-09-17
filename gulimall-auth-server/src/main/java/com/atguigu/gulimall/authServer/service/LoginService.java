package com.atguigu.gulimall.authServer.service;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authServer.exception.TooManySmsCodeRequestException;
import com.atguigu.gulimall.authServer.feign.MemberFeignService;
import com.atguigu.gulimall.authServer.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.authServer.vo.UserLoginVo;
import com.atguigu.gulimall.authServer.vo.UserRegistVo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class LoginService {

    private final ThirdPartyFeignService thirdPartyFeignService;

    private final StringRedisTemplate stringRedisTemplate;

    private final MemberFeignService memberFeignService;

    public static final long SMS_REQUEST_INTERVAL_MS = Duration.ofSeconds(60).toMillis();

    public void sendCode(String phone) {
        String cachedCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(cachedCode)) {
            long cachedCodeTime = Long.parseLong(cachedCode.split("_")[1]);
            if (System.currentTimeMillis() - cachedCodeTime < SMS_REQUEST_INTERVAL_MS) {
                throw new TooManySmsCodeRequestException();
            }
        }

        String code = RandomStringUtils.randomNumeric(6) + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        thirdPartyFeignService.smsToConsole(phone, code.split("_")[0]);
    }

    public boolean validateSmsCode(String phone, String code) {
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(s) && code.equals(s.split("_")[0])) {
            stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
            return true;
        }
        return false;
    }

    public R regist(UserRegistVo userRegistVo) {
        return memberFeignService.regist(userRegistVo);
    }

    public R login(UserLoginVo vo) {
        return memberFeignService.login(vo);
    }
}
