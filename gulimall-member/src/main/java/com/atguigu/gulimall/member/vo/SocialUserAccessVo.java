package com.atguigu.gulimall.member.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SocialUserAccessVo {
    private String accessToken;
    private String uid;
    private String isRealName;
    private long expiresIn;
    private String remindIn;
}
