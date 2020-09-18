package com.atguigu.gulimall.cart.dao;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CartRedisDao {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String CART_PREFIX = "gulimall:cart:";

    private BoundHashOperations<String, String, Object> cartOps;

    private BoundHashOperations<String, String, Object> tempCartOps = null;

    @PostConstruct
    private void init() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoThreadLocal.get();
        if (userInfoTo.getUserId() != null) {
            cartOps = stringRedisTemplate.boundHashOps(CART_PREFIX + userInfoTo.getUserId());
            tempCartOps = stringRedisTemplate.boundHashOps(CART_PREFIX + userInfoTo.getUserKey());
        } else {
            cartOps = stringRedisTemplate.boundHashOps(CART_PREFIX + userInfoTo.getUserKey());
        }
    }

    public void setCartItem(String skuId, CartItem cartItem) {
        cartOps.put(skuId, JSON.toJSONString(cartItem));
    }

    public void remoteCartItem(String skuId) {
        cartOps.delete(skuId);
    }

    public CartItem getCartItem(String skuId) {
        final String json = (String) cartOps.get(skuId);
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json, CartItem.class);
        }
        return null;
    }

    public List<CartItem> getAllItems() {
        return Objects.requireNonNull(cartOps.values()).stream()
            .map((item) -> JSON.parseObject((String) item, CartItem.class))
            .collect(Collectors.toList());
    }

    public void clear() {
        stringRedisTemplate.delete(cartOps.getKey());
    }

    public List<CartItem> getTempCardItems() {
        if (tempCartOps != null) {
            return Objects.requireNonNull(tempCartOps.values()).stream()
                .map((item) -> JSON.parseObject((String) item, CartItem.class))
                .collect(Collectors.toList());
        }
        return null;
    }

    public void clearTemp() {
        if (tempCartOps != null) {
            stringRedisTemplate.delete(tempCartOps.getKey());
        }
    }
}
