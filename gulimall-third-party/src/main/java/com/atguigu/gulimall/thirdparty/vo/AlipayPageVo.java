package com.atguigu.gulimall.thirdparty.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlipayPageVo {
    private String charset;
    private String alipay_sdk;
    private String method;
    private String sign;
    private String format;
    private String return_url;
    private String notify_url;
    private String version;
    private String app_id;
    private String sign_type;
    private String timestamp;
    private AliBizContentVo biz_content;
}
