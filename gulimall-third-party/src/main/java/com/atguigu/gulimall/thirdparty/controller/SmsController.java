package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;


@RequestMapping("/sms")
@RestController
public class SmsController {
    @Autowired
    private SmsComponent smsComponent;

    @Value("${spring.cloud.alicloud.sms.appcode}")
    private String appcode;

    @GetMapping("/sendcode")
    public R smsToConsole(@RequestParam("phone") String phone, @RequestParam("code") String code,
                          HttpServletRequest request, HttpServletResponse response) {
        // String[] split = Optional.ofNullable(request.getHeader("Authorization")).orElse("").split("\\s");
        // if (split.length != 2 || !split[1].equals(appcode)) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     return null;
        // }
        if (!code.matches("\\d{6}")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return R.SmsResp.FAIL("code格式错误");
        }
        if (!phone.matches("\\d{11}")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return R.SmsResp.FAIL("phone格式错误");
        }
        System.out.println("SMS: " + phone + " " + code);
        return R.SmsResp.OK();
    }

    // @GetMapping("/get")
    // public String get() {
    //     return "hello!!22";
    // }

    // @Data
    // private static class Param {
    //     private String code;
    //     private String phone;
    //     private String sign;
    //     private String skin;
    // }

    @Data
    @AllArgsConstructor
    private static class Resp {
        private String Message;
        private String Code;
        private String RequestId;
        private String BizId;

        public static final Resp OK = new Resp("OK", "OK", "", "");
    }

}
