package com.atguigu.gulimall.authServer.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authServer.feign.MemberFeignService;
import com.atguigu.gulimall.authServer.vo.SocialUser;
import lombok.AllArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class OAuth2Service {
    private final MemberFeignService memberFeignService;

    public R weiboGetSocialUser(String code) {
        String queryStr = URLEncodedUtils.format(Arrays.asList(
            new BasicNameValuePair("client_id", "443706065"),
            new BasicNameValuePair("client_secret", "2b709f78397546b2eed0b394e3dcfef6"),
            new BasicNameValuePair("grant_type", "authorization_code"),
            new BasicNameValuePair("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success"),
            new BasicNameValuePair("code", code)
        ), "UTF-8");

        try {
            HttpResponse response = Request.Post("https://api.weibo.com/oauth2/access_token?" + queryStr).execute().returnResponse();

            if (response.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(response.getEntity());
                final SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
                return memberFeignService.oauthLogin(socialUser);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
