package com.atguigu.gulimall.authServer.dao;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.authServer.vo.SmsCodeVo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Repository
@AllArgsConstructor
public class SmsCodeRedisDao {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean validateAndDelete(String phone, String code) {
        final String json = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(json)) {
            final SmsCodeVo cachedCode = JSON.parseObject(json, SmsCodeVo.class);
            if (cachedCode.getCode().equals(code)) {
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
                return true;
            }
            return false;
        }
        return false;
    }

    public SmsCodeVo getExistCode(String phone) {
        final String cachedCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(cachedCode)) {
            return JSON.parseObject(cachedCode, SmsCodeVo.class);
        }
        return null;
    }

    public SmsCodeVo getNewCode(String phone, Duration resendInterval, Duration validFor) {
        final SmsCodeVo newCode = new SmsCodeVo().setCode(generateCode()).setResendAfter(Instant.now().plus(resendInterval));
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, JSON.toJSONString(newCode), validFor);
        return newCode;
    }

    private String generateCode() {
        return RandomStringUtils.randomNumeric(6);
    }
}
