package com.atguigu.gulimall.authServer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authServer.feign.MemberFeignService;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.authServer.service.OAuth2Service;
import com.atguigu.gulimall.authServer.vo.SocialUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Controller
@AllArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oAuth2Service;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession httpSession) {

        R oauthLogin = oAuth2Service.weiboGetSocialUser(code);
        if (oauthLogin.getCode() == 0) {
            MemberRespVo data = oauthLogin.getData(new TypeReference<MemberRespVo>() {});
            log.info("登陆成功：{}", data.toString());

            httpSession.setAttribute(AuthServerConstant.LOGIN_USER, data);

            return "redirect:http://gulimall.com";
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
