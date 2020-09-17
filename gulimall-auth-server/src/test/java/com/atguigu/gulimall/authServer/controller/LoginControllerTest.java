package com.atguigu.gulimall.authServer.controller;

import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.ControllerTestConfig;
import com.atguigu.gulimall.authServer.service.LoginService;
import com.atguigu.gulimall.authServer.vo.UserRegistVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static com.atguigu.common.Rmatcher.Rm;
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
        mockMvc.perform(get("/sms/sendcode"))
            .andExpect(Rm().statusEquals(200));
    }

    @Test
    void registWithFieldErrorShouldRedirectToRegist() throws Exception {
        final UserRegistVo registVo = createUserRegistVo().setPhone("123");

        mockMvc.perform(post("/regist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(registVo, Map.class))))
            .andExpect(Rm().flashMapSize("errors", 1))
            .andExpect(Rm().flashMapHasAttr("errors", Collections.singletonMap("phone", "手机格式不正确")))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/reg.html"));
    }

    @Test
    void registWithWrongSmsCodeShouldRedirectToRegist() throws Exception {
        final UserRegistVo registVo = createUserRegistVo().setCode("123123");

        mockMvc.perform(post("/regist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(objectMapper.convertValue(registVo, Map.class))))
            .andExpect(Rm().flashMapSize("errors", 1))
            .andExpect(Rm().flashMapHasAttr("errors", Collections.singletonMap("code", "验证码错误")))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/reg.html"));
    }

    UserRegistVo createUserRegistVo() {
        return new UserRegistVo().setUserName("hellotommy").setPassword("Abc123456").setPhone("19000000000").setCode("123456");
    }
}
