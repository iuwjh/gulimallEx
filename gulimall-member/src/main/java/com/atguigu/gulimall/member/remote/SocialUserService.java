package com.atguigu.gulimall.member.remote;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.member.remote.vo.SocialUserVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessVo;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;


@Service
public class SocialUserService {
    public static final String WEIBO_URL = "https://api.weibo.com/2/users/show.json?";
    @Autowired
    RestTemplate restTemplate;

    public SocialUserVo weiboRegist(SocialUserAccessVo socialUserAccessVo) throws URISyntaxException {
        List<BasicNameValuePair> nameValuePairs = Arrays.asList(
            new BasicNameValuePair("access_token", socialUserAccessVo.getAccessToken()),
            new BasicNameValuePair("uid", socialUserAccessVo.getUid())
        );
        String queryStr = URLEncodedUtils.format(nameValuePairs, "UTF-8");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(WEIBO_URL + queryStr, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        JSONObject jsonObject = JSON.parseObject(responseEntity.toString());

        String name = jsonObject.getString("name");
        String gender = jsonObject.getString("gender");

        return new SocialUserVo().setNickname(name).setGender(gender);
    }
}
