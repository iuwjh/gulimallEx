package com.atguigu.gulimall.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallGatewayApplicationTests {

    @Value("${guli.hello}")
    String hello;

    @Test
    public void test1() {
        System.out.println(hello);
    }

    @Test
    public void contextLoads() {
    }

}
