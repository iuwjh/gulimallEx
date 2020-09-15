package com.atguigu.gulimall.member.remote.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SocialUserVo {
    private String nickname;
    // m -> 男
    private String gender;
}
