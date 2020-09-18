package com.atguigu.gulimall.authServer.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.ControllerTestConfig;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.authServer.service.LoginService;
import com.atguigu.gulimall.authServer.service.OAuth2Service;
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

import static com.atguigu.common.Rmatcher.Rm;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = OAuth2Controller.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class OAuth2ControllerTest {
    @MockBean
    OAuth2Service oAuth2Service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "";

    @Test
    void weiboLoginFail() throws Exception {
        Mockito.when(oAuth2Service.weiboGetSocialUser(anyString())).thenReturn(R.error(1, ""));

        mockMvc.perform(get(BASE_URL + "/oauth2.0/weibo/success")
            .queryParam("code", "123456"))
            .andExpect(Rm().redirectedTo("http://auth.gulimall.com/login.html"));
    }

    @Test
    void weiboLoginSucceed() throws Exception {
        final MemberRespVo memberRespVo = new MemberRespVo().setUsername("hitherejade");
        Mockito.when(oAuth2Service.weiboGetSocialUser(anyString())).thenReturn(R.ok().setData(memberRespVo));

        mockMvc.perform(get(BASE_URL + "/oauth2.0/weibo/success")
            .queryParam("code", "123456"))
            .andExpect(MockMvcResultMatchers.request().sessionAttribute(AuthServerConstant.LOGIN_USER, memberRespVo))
            .andExpect(Rm().redirectedTo("http://gulimall.com"));
    }
}
