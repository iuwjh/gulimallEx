package com.atguigu.gulimall.seckill.config;

// @Configuration
public class SeckillSentinelConfig {

    // public BlockExceptionHandler blockExceptionHandler() {
    //     return new BlockExceptionHandler() {
    //         @Override
    //         public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
    //             R error = R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMsg());
    //             response.setCharacterEncoding("UTF-8");
    //             response.setContentType("application/json");
    //             response.setStatus(BizCodeEnume.TOO_MANY_REQUEST.getStatus());
    //             response.getWriter().write(JSON.toJSONString(error));
    //         }
    //     };
    // }
}
