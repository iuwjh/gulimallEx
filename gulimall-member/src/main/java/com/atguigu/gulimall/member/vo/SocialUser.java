package com.atguigu.gulimall.member.vo;

import lombok.Data;

@Data
public class SocialUser {
    private String accessToken;
    private String uid;
    private String isRealName;
    private long expiresIn;
    private String remindIn;
}
