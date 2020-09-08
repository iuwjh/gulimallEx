package com.atguigu.gulimall.authServer.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authServer.vo.SocialUser;
import com.atguigu.gulimall.authServer.vo.UserLoginVo;
import com.atguigu.gulimall.authServer.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser);

}
