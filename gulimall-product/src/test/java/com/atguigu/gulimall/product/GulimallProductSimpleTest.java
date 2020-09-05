package com.atguigu.gulimall.product;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

@RunWith(JUnit4.class)
public class GulimallProductSimpleTest {

    @Test
    public void test1() {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>() {{
            put("a1", Collections.singletonList("喜喜"));
            put("a2", Collections.singletonList("哎哎"));
        }};
        String s = JSON.toJSONString(map);
        System.out.println(s);
    }
}
