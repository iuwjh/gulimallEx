package com.atguigu.gulimall.authServer.dao;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.EmbeddedRedisConfig;
import com.atguigu.common.config.RedisConfig;
import com.atguigu.gulimall.authServer.vo.SmsCodeVo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("testWithoutSQL")
public class SmsCodeRedisDaoTest {
    @Autowired
    SmsCodeRedisDao smsCodeRedisDao;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setup() {
        CommonTestHelper.clearRedis(stringRedisTemplate);
    }

    @Test
    void getNewCode() throws Exception {
        final SmsCodeVo newCode = smsCodeRedisDao.getNewCode("18000000000", Duration.ZERO, Duration.ofDays(1));
        assertThat(newCode).isNotNull();
    }

    @Test
    void getExistCode() throws Exception {
        final String phone = "18000000000";
        final SmsCodeVo smsCodeVo = smsCodeRedisDao.getNewCode(phone, Duration.ZERO, Duration.ofDays(1));
        assertThat(smsCodeRedisDao.getExistCode(phone)).isEqualToComparingFieldByField(smsCodeVo);
    }

    @Test
    void validateAndDelete() throws Exception {
        final String phone = "18000000000";
        final SmsCodeVo newCode = smsCodeRedisDao.getNewCode(phone, Duration.ZERO, Duration.ofDays(1));

        assertThat(smsCodeRedisDao.getExistCode(phone)).isNotNull();
        smsCodeRedisDao.validateAndDelete(phone, newCode.getCode());
        assertThat(smsCodeRedisDao.getExistCode(phone)).isNull();

    }

    private SmsCodeVo createSmsCodeVo() {
        return new SmsCodeVo().setCode("333333").setResendAfter(Instant.now());
    }
}
