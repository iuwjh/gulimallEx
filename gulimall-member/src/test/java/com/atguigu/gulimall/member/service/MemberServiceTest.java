package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.controller.vo.MemberRegistVo;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PasswordFormatException;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.remote.SocialUserService;
import com.atguigu.gulimall.member.service.impl.MemberServiceImpl;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final String PASSWORD_VALID = "Abc123456";

    private static final Supplier<MemberRegistVo> validRegist = () ->
        new MemberRegistVo().setPhone("17004040404").setUserName("USERNAME_NOT_EXIST").setPassword(PASSWORD_VALID);
    private static final Supplier<MemberRegistVo> invalidRegist = () ->
        new MemberRegistVo().setPhone("18200200200").setUserName("USERNAME_EXIST").setPassword("123");

    private static final Supplier<MemberLoginVo> validLoginByPhone = () ->
        new MemberLoginVo().setLoginAcct("18200200200").setPassword(PASSWORD_VALID);
    private static final Supplier<MemberLoginVo> validLoginByUsername = () ->
        new MemberLoginVo().setLoginAcct("USERNAME_EXIST").setPassword(PASSWORD_VALID);
    private static final Supplier<MemberEntity> validLoginEntity = () ->
        new MemberEntity()
            .setUsername(validLoginByUsername.get().getLoginAcct())
            .setMobile(validLoginByPhone.get().getLoginAcct())
            .setPassword(new BCryptPasswordEncoder().encode(validLoginByUsername.get().getPassword()));

    private static final Supplier<SocialUserAccessVo> validSocialLogin = () ->
        new SocialUserAccessVo().setUid("SOCIAL_UID_EXISTED").setAccessToken("SOCIAL_ACCESS_TOKEN_EXISTED").setExpiresIn(Duration.ofDays(30L).toMillis());
    private static final Supplier<MemberEntity> validSocialLoginEntity = () ->
        new MemberEntity()
            .setSocialUid(validSocialLogin.get().getUid())
            .setAccessToken(validSocialLogin.get().getAccessToken())
            .setExpiresIn(validSocialLogin.get().getExpiresIn());

    @Mock
    private MemberDao memberDao;

    @Mock
    private MemberLevelDao memberLevelDao;

    @Mock
    private SocialUserService socialUserService;

    private MemberService memberService;

    @BeforeEach
    void setup() {
        this.memberService = Mockito.spy(new MemberServiceImpl(memberDao, memberLevelDao, socialUserService));
    }

    /**
     * 测试分页
     */
    // @Test
    void queryPage() {
        Map<String, Object> map = new HashMap<>();
        map.put("page", "1");
        map.put("limit", "10");
        PageUtils pageUtils = memberService.queryPage(map);
        assertThat(pageUtils.getList()).hasSize(2);
    }

    @Nested
    class RegistTest {

        @BeforeEach
        void setup() {
            Mockito.when(memberLevelDao.getDefaultLevel()).thenReturn(new MemberLevelEntity().setId(1L));
            Mockito.lenient().when(memberDao.checkPhoneUnique(Mockito.anyString()))
                .thenAnswer(invocation -> !invocation.getArgument(0).equals(invalidRegist.get().getPhone()));
            Mockito.lenient().when(memberDao.checkUsernameUnique(Mockito.anyString()))
                .thenAnswer(invocation -> !invocation.getArgument(0).equals(invalidRegist.get().getUserName()));
        }

        /**
         * 注册手机存在 抛异常
         */
        @Test
        void registWithExistingPhoneShouldThrow() {
            MemberRegistVo regisVo = validRegist.get().setPhone(invalidRegist.get().getPhone());
            assertThatThrownBy(() -> {
                memberService.regist(regisVo);
            }).isInstanceOf(PhoneExistException.class);
        }

        /**
         * 注册用户名存在 抛异常
         */
        @Test
        void registWithExistingUsernameShouldThrow() {
            MemberRegistVo regisVo = validRegist.get().setUserName(invalidRegist.get().getUserName());
            assertThatThrownBy(() -> {
                memberService.regist(regisVo);
            }).isInstanceOf(UsernameExistException.class);
        }

        /**
         * 注册密码格式错误 抛异常
         */
        @Test
        void registWithInvalidPasswordShouldThrow() {
            MemberRegistVo regisVo = validRegist.get().setPassword(invalidRegist.get().getPassword());
            assertThatThrownBy(() -> {
                memberService.regist(regisVo);
            }).isInstanceOf(PasswordFormatException.class);
        }

        /**
         * 注册成功
         */
        @Test
        void registShouldSucceed() {
            assertThatCode(() -> {
                memberService.regist(validRegist.get());
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    class LoginTest {
        @BeforeEach
        void setup() {
            Mockito.when(memberDao.findByLoginAcct(Mockito.anyString())).thenAnswer(invocation ->
                Arrays.asList(validLoginByUsername.get().getLoginAcct(), validLoginByPhone.get().getLoginAcct())
                    .contains(invocation.getArgument(0)) ? validLoginEntity.get() : null);
            //TODO 重构：移除Service类中的baseMapper域
            ReflectionTestUtils.setField(memberService, "baseMapper", memberDao);
        }

        /**
         * 登录用户名不存在 返回空
         */
        @Test
        void loginWithNonExistingUsernameShouldFail() {
            MemberEntity login = memberService.login(validLoginByUsername.get().setLoginAcct(validLoginByUsername.get().getLoginAcct() + 1));
            assertThat(login).isNull();
        }

        /**
         * 登录手机不存在 返回空
         */
        @Test
        void loginWithNonExistingPhoneShouldFail() {
            MemberEntity login = memberService.login(validLoginByPhone.get().setLoginAcct(validLoginByPhone.get().getLoginAcct() + 1));
            assertThat(login).isNull();
        }

        /**
         * 登录密码错误 返回空
         */
        @Test
        void loginWithWrongPasswordShouldFail() {
            MemberEntity login = memberService.login(validLoginByUsername.get().setPassword(validLoginByUsername.get().getPassword() + 1));
            assertThat(login).isNull();
        }

        /**
         * 用户名登录成功
         */
        @Test
        void loginWithExistingUsernameShouldSucceed() {
            MemberEntity login = memberService.login(validLoginByUsername.get());
            assertThat(login).isNotNull();
            assertThat(login.getUsername()).isNotNull().isEqualTo(validLoginByUsername.get().getLoginAcct());
        }

        /**
         * 手机登录成功
         */
        @Test
        void loginWithExistingPhoneShouldSucceed() {
            MemberEntity login = memberService.login(validLoginByPhone.get());
            assertThat(login).isNotNull();
            assertThat(login.getMobile()).isNotNull().isEqualTo(validLoginByPhone.get().getLoginAcct());
        }
    }

    @Nested
    class LoginSocialTest {
        @BeforeEach
        void setup() throws URISyntaxException {
            Mockito.when(memberDao.findBySocialUid(Mockito.anyString())).thenAnswer(invocation ->
                invocation.getArgument(0).equals(validSocialLogin.get().getUid()) ? validSocialLoginEntity.get() : null);
        }

        /**
         * 微博社交登录成功
         */
        @Test
        void loginSocialWithExistingUidShouldSucceed() {
            MemberEntity loginSocial = memberService.loginSocial(validSocialLogin.get());
            assertThat(loginSocial).isNotNull();
            assertThat(loginSocial.getSocialUid()).isNotNull().isEqualTo(validSocialLogin.get().getUid());
            assertThat(loginSocial.getAccessToken()).isNotNull().isEqualTo(validSocialLogin.get().getAccessToken());
            assertThat(loginSocial.getExpiresIn()).isNotNull().isEqualTo(validSocialLogin.get().getExpiresIn());
        }
    }
}
