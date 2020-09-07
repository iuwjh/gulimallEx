package com.atguigu.gulimall.member;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"dev", "test"})
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {
    }

}
