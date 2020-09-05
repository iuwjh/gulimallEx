package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;

    public static final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            CartItem cartItem = new CartItem();

            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.skuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setSkuId(skuId);
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());

            }, executor);

            CompletableFuture<Void> attrFuture = CompletableFuture.runAsync(() -> {
                List<String> attrList = productFeignService.infoBySkuId(skuId).stream()
                        .map((item) -> String.format("%s: %s", item.getAttrName(), item.getAttrValue()))
                        .collect(Collectors.toList());
                cartItem.setSkuAttr(attrList);
            }, executor);

            CompletableFuture.allOf(skuInfoFuture, attrFuture).get();
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            CartItem oldItem = JSON.parseObject(res, CartItem.class);
            oldItem.setCount(oldItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(oldItem));
            return oldItem;
        }


    }

    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());

        return JSON.parseObject(s, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.userInfoThreadLocal.get();
        if (userInfoTo.getUserId() != null) {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();

            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null) {
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                clearCart(tempCartKey);
            }

            cart.setItems(getCartItems(cartKey));

        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }


        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> currentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoThreadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;

        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            return getCartItems(cartKey).stream()
                    .filter(CartItem::getCheck)
                    .peek((cartItem) -> {
                        R r = productFeignService.getPrice(cartItem.getSkuId());
                        cartItem.setPrice(r.getData(new TypeReference<BigDecimal>() {
                        }));
                    })
                    .collect(Collectors.toList());

        }

    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        return Optional.ofNullable(hashOps.values())
                .orElse(Collections.emptyList())
                .stream()
                .map((e) -> JSON.parseObject((String) e, CartItem.class))
                .collect(Collectors.toList());
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoThreadLocal.get();
        String cartKey;
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        return stringRedisTemplate.boundHashOps(cartKey);
    }

}
