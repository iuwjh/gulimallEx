package com.atguigu.gulimall.order;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.vo.PayVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {
    // @Autowired
    // RabbitTemplate rabbitTemplate;

    @Autowired
    AlipayTemplate alipayTemplate;

    @Test
    public void contextLoads() throws AlipayApiException {
        // rabbitTemplate.convertAndSend("exchange.direct", "gulixueyuan.news", "Hello!");
        // rabbitTemplate.getConnectionFactory().createConnection().close();

        alipayTemplate.pay(new PayVo());
    }
}
