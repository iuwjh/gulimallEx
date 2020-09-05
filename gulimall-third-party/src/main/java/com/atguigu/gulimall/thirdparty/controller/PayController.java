package com.atguigu.gulimall.thirdparty.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.thirdparty.vo.AliAsyncNotifyVo;
import com.atguigu.gulimall.thirdparty.vo.AlipayPageVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@RequestMapping("/pay")
@Controller
public class PayController {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ThreadPoolExecutor executor;

    @PostMapping(value = "/alipayPage.html")
    public String alipayPage(AlipayPageVo vo, HttpServletRequest request, HttpSession session) {

        // executor.execute(() -> {
        //     System.out.println("Alipay: 发送支付异步通知");
        //     int retries = 3;
        //     int wait = 20000;
        //     for (int i = 0; i < retries; i++) {
        //         try {
        //             // List<BasicNameValuePair> params = objectMapper.convertValue(asyncNotifyVo, new TypeReference<Map<String, String>>() {
        //             // }).entrySet().stream().map((e) -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());
        //
        //             HttpResponse resp = Request.Post(vo.getNotify_url())
        //                     .bodyString(JSON.toJSONString(asyncNotifyVo), ContentType.APPLICATION_JSON)
        //                     .execute().returnResponse();
        //             String result = EntityUtils.toString(resp.getEntity());
        //             if ("success".equals(result)) {
        //                 return;
        //             }
        //             Thread.sleep(wait);
        //         } catch (IOException | InterruptedException e) {
        //             e.printStackTrace();
        //         }
        //     }
        //     System.out.println("Alipay: 支付异步通知失败，重试次数用完");
        // });

        session.setAttribute("payvo", vo);

        return "alipayPage";
    }

    @GetMapping(value = "/alipayPage.html")
    public String getAipayPage() {
        return "alipayPage";
    }

    @PostMapping("/handleAlipay")
    public String handleAlipay(HttpSession session) throws ParseException {
        AlipayPageVo vo = (AlipayPageVo) session.getAttribute("payvo");

        AliAsyncNotifyVo asyncNotifyVo = new AliAsyncNotifyVo();

        DateTimeFormatter gmtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("GMT"));
        LocalDateTime orderTime = LocalDateTime.from(gmtFormatter.parse(vo.getTimestamp()));
        LocalDateTime currentTime = LocalDateTime.now();

        String timeout_express = Optional.ofNullable(vo.getBiz_content().getTimeout_express()).orElse("10m");

        int multiplier = Integer.parseUnsignedInt(timeout_express.substring(0, timeout_express.length() - 1));
        String unitTime = timeout_express.substring(timeout_express.length() - 1).toLowerCase()
                .replace("h", "3600")
                .replace("m", "60")
                .replace("s", "1");
        LocalDateTime payEndtime = currentTime.plusSeconds(multiplier * Integer.parseUnsignedInt(unitTime));

        if (currentTime.isAfter(payEndtime)) {
            System.out.println("Alipay: 支付超时");
            return "alipayFail";
        }

        asyncNotifyVo.setGmt_create(gmtFormatter.format(orderTime));
        asyncNotifyVo.setGmt_payment(gmtFormatter.format(currentTime));
        asyncNotifyVo.setCharset(vo.getCharset());
        asyncNotifyVo.setVersion(vo.getVersion());
        asyncNotifyVo.setSign(vo.getSign());
        asyncNotifyVo.setSign_type(vo.getSign_type());
        asyncNotifyVo.setApp_id(vo.getApp_id());
        asyncNotifyVo.setAuth_app_id(vo.getApp_id());
        asyncNotifyVo.setTrade_no(RandomStringUtils.randomNumeric(16));

        asyncNotifyVo.setOut_trade_no(vo.getBiz_content().getOut_trade_no());
        asyncNotifyVo.setTrade_status("TRADE_SUCCESS");
        asyncNotifyVo.setNotify_time(Date.valueOf(currentTime.toLocalDate()));
        asyncNotifyVo.setNotify_type("trade_status_sync");
        asyncNotifyVo.setNotify_id("321321");
        asyncNotifyVo.setBuyer_id("111111");
        asyncNotifyVo.setFund_bill_list(String.format("[{\"amount\":\"%s\", \"fundChannel\":\"%s\"}]", vo.getBiz_content().getTotal_amount(), "ALIPAYACCOUNT"));

        asyncNotifyVo.setSubject(vo.getBiz_content().getSubject());
        asyncNotifyVo.setBody(vo.getBiz_content().getBody());
        asyncNotifyVo.setTotal_amount(vo.getBiz_content().getTotal_amount());
        asyncNotifyVo.setBuyer_pay_amount(vo.getBiz_content().getTotal_amount());
        asyncNotifyVo.setReceipt_amount(vo.getBiz_content().getTotal_amount());
        asyncNotifyVo.setInvoice_amount(vo.getBiz_content().getTotal_amount());
        asyncNotifyVo.setPoint_amount("0");
        asyncNotifyVo.setSeller_id("123456");

        executor.execute(() -> {
            System.out.println("Alipay: 发送支付异步通知");
            int retries = 3;
            int wait = 20000;
            for (int i = 0; i < retries; i++) {
                try {
                    // List<BasicNameValuePair> params = objectMapper.convertValue(asyncNotifyVo, new TypeReference<Map<String, String>>() {
                    // }).entrySet().stream().map((e) -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());

                    HttpResponse resp = Request.Post(vo.getNotify_url())
                            .bodyString(JSON.toJSONString(asyncNotifyVo), ContentType.APPLICATION_JSON)
                            .execute().returnResponse();
                    String result = EntityUtils.toString(resp.getEntity());
                    if ("success".equals(result)) {
                        return;
                    }
                    Thread.sleep(wait);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Alipay: 支付异步通知失败，重试次数用完");
        });

        return "redirect:" + vo.getReturn_url();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

}
