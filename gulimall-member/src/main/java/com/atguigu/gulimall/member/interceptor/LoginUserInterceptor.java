package com.atguigu.gulimall.member.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUserThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", requestURI);
        if (match) {
            return true;
        }

        MemberRespVo member = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            loginUserThreadLocal.set(member);
            return true;
        } else {
            request.getSession().setAttribute("msg", "请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
