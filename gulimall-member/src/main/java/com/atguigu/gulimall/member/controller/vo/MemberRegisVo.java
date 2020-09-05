package com.atguigu.gulimall.member.controller.vo;

import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class MemberRegisVo {
    @NotEmpty(message = "用户名必须填写")
    @Length(min = 6,max = 18,message = "用户名必须是6-18位字符")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码必须是6-18位字符")
    private String password;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "\\d{11}", message = "手机格式不正确")
    private String phone;
}
