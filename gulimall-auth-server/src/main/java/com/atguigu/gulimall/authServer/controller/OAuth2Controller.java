package com.atguigu.gulimall.authServer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authServer.feign.MemberFeignService;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.authServer.vo.SocialUser;
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
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession httpSession) throws IOException {
        // Map<String, String> paramMap = new HashMap<>();
        // paramMap.put("client_id", "443706065");
        // paramMap.put("client_secret", "2b709f78397546b2eed0b394e3dcfef6");
        // paramMap.put("grant_type", "authorization_code");
        // paramMap.put("redirect_uri", "http://gulimall.com/success");
        // paramMap.put("code", "733a05b07845199fde8974a1fbbd8d73");

        // String queryStr = paramMap.entrySet().stream()
        //         .map(e -> e.getKey() + "=" + e.getValue())
        //         .reduce("", (s, e) -> s + "&" + e);

        // URL url = new URL("https://api.weibo.com/oauth2/access_token?" + queryStr);
        // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // conn.setRequestMethod("POST");

        String queryStr = URLEncodedUtils.format(Arrays.asList(
                new BasicNameValuePair("client_id", "443706065"),
                new BasicNameValuePair("client_secret", "2b709f78397546b2eed0b394e3dcfef6"),
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success"),
                new BasicNameValuePair("code", code)
        ), "UTF-8");

        HttpResponse response = Request.Post("https://api.weibo.com/oauth2/access_token?" + queryStr).execute().returnResponse();

        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberRespVo data = oauthLogin.getData(new TypeReference<MemberRespVo>() {
                });
                log.info("登陆成功：{}", data.toString());

                httpSession.setAttribute(AuthServerConstant.LOGIN_USER, data);

                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }

        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

        // System.out.println(queryStr);
        // System.out.println(code);

        // return "redirect:http://gulimall.com";
    }
}
