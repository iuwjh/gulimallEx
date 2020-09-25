package com.atguigu.gulimall.authServer.controller;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.authServer.service.LoginService;
import com.atguigu.gulimall.authServer.vo.UserLoginVo;
import com.atguigu.gulimall.authServer.vo.UserRegistVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.Map;

import static com.atguigu.common.Rmatcher.Rm;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = LoginController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class LoginControllerTest {
    @MockBean
    LoginService loginService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "";

    @Test
    void sendCode() throws Exception {
        mockMvc.perform(get(BASE_URL + "/sms/sendcode")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("phone", "18000000000"))
            .andExpect(Rm().statusEquals(200));
    }

    @Test
    void registWithFieldErrorShouldRedirectToRegist() throws Exception {
        final UserRegistVo registVo = createUserRegistVo().setPhone("123");

        mockMvc.perform(post(BASE_URL + "/regist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(registVo, Map.class))))
            .andExpect(Rm().flashmapMapSize("errors", 1))
            .andExpect(Rm().flashmapHasAttr("errors", Collections.singletonMap("phone", "手机格式不正确")))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/reg.html"));
    }

    @Test
    void registWithWrongSmsCodeShouldRedirectToRegist() throws Exception {
        final UserRegistVo registVo = createUserRegistVo().setCode("123123");
        Mockito.when(loginService.validateSmsCode(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post(BASE_URL + "/regist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(registVo, Map.class))))
            .andExpect(Rm().flashmapMapSize("errors", 1))
            .andExpect(Rm().flashmapHasAttr("errors", Collections.singletonMap("code", "验证码错误")))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/reg.html"));
    }

    @Test
    void registRejectedByRemoteService() throws Exception {
        final UserRegistVo registVo = createUserRegistVo();
        Mockito.when(loginService.validateSmsCode(anyString(), anyString())).thenReturn(true);
        Mockito.when(loginService.regist(any())).thenReturn(R.error(1, "").put("msg", "fail"));

        mockMvc.perform(post(BASE_URL + "/regist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(registVo, Map.class))))
            .andExpect(Rm().flashmapMapSize("errors", 1))
            .andExpect(Rm().flashmapHasAttr("errors", Collections.singletonMap("msg", "fail")))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/reg.html"));
    }

    @Test
    void registSucceed() throws Exception {
        final UserRegistVo registVo = createUserRegistVo();
        Mockito.when(loginService.validateSmsCode(anyString(), anyString())).thenReturn(true);
        Mockito.when(loginService.regist(any())).thenReturn(R.ok());

        mockMvc.perform(post(BASE_URL + "/regist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(registVo, Map.class))))
            .andExpect(Rm().flashmapSize(1))
            .andExpect(Rm().flashmapHasAttr("username", registVo.getUserName()))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/login.html"));
    }

    @Test
    void loginPageWithNotLoggedInUserRedirectToLogin() throws Exception {
        mockMvc.perform(get(BASE_URL + "/login.html"))
            .andExpect(MockMvcResultMatchers.view().name("login"));
    }

    @Test
    void loginPageWithLoggedInUserRedirectToHome() throws Exception {
        mockMvc.perform(get(BASE_URL + "/login.html")
            .sessionAttr(AuthServerConstant.LOGIN_USER, new Object()))
            .andExpect(Rm().redirectedTo("http://gulimall.com"));
    }

    @Test
    void loginRejectedByRemoteService() throws Exception {
        final UserLoginVo loginVo = createUserLoginVo();
        Mockito.when(loginService.login(any())).thenReturn(R.error(1, "").put("msg", "fail"));

        mockMvc.perform(post(BASE_URL + "/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(loginVo, Map.class))))
            .andExpect(Rm().flashmapSize(1))
            .andExpect(Rm().flashmapHasAttr("errors", Collections.singletonMap("msg", "fail")))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/login.html"));
    }

    @Test
    void loginSucceed() throws Exception {
        final UserLoginVo loginVo = createUserLoginVo();
        Mockito.when(loginService.login(any()))
            .thenReturn(R.ok().put("msg", "fail").setData(new MemberRespVo()));

        mockMvc.perform(post(BASE_URL + "/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(loginVo, Map.class))))
            .andExpect(Rm().flashmapSize(0))
            .andExpect(Rm().redirectedTo("http://gulimall.com"));
    }

    private UserRegistVo createUserRegistVo() {
        return new UserRegistVo().setUserName("hellotommy").setPassword("Abc123456").setPhone("19000000000").setCode("123456");
    }

    private UserLoginVo createUserLoginVo() {
        return new UserLoginVo().setLoginAcct("byetommy").setPassword("Abc888888");
    }
}
