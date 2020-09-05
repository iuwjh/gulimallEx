package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> userInfoThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        MemberRespVo member = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            userInfoTo.setUserId(member.getId());
        }

        Optional.ofNullable(request.getCookies()).flatMap(cs -> Arrays.stream(cs)
                .filter((c) -> c.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME))
                .findFirst())
                .ifPresent((c) -> {
                    userInfoTo.setUserKey(c.getValue());
                    userInfoTo.setTempUser(true);
                });

        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }


        userInfoThreadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = userInfoThreadLocal.get();
        if (!userInfoTo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
