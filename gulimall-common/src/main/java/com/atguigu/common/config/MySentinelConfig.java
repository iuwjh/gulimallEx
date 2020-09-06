package com.atguigu.common.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class MySentinelConfig {

    public BlockExceptionHandler blockExceptionHandler() {
        return new BlockExceptionHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
                R error = R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMsg());
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.setStatus(BizCodeEnume.TOO_MANY_REQUEST.getStatus());
                response.getWriter().write(JSON.toJSONString(error));
            }
        };
    }

}