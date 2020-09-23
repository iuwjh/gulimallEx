package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.dao.CartRedisDao;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductFeignService productFeignService;

    private final CartRedisDao cartRedisDao;

    private final ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        final CartItem oldItem = cartRedisDao.getCartItem(skuId.toString());

        if (oldItem != null) {
            oldItem.setCount(oldItem.getCount() + num);
            cartRedisDao.setCartItem(skuId.toString(), oldItem);
            return oldItem;
        } else {
            CartItem newItem = new CartItem();

            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.skuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                newItem.setSkuId(skuId);
                newItem.setCount(num);
                newItem.setCheck(true);
                newItem.setTitle(data.getSkuTitle());
                newItem.setImage(data.getSkuDefaultImg());
                newItem.setPrice(data.getPrice());

            }, executor);

            CompletableFuture<Void> attrFuture = CompletableFuture.runAsync(() -> {
                List<String> attrList = productFeignService.infoBySkuId(skuId).stream()
                    .map((item) -> String.format("%s: %s", item.getAttrName(), item.getAttrValue()))
                    .collect(Collectors.toList());
                newItem.setSkuAttr(attrList);
            }, executor);

            CompletableFuture.allOf(skuInfoFuture, attrFuture).get();

            cartRedisDao.setCartItem(skuId.toString(), newItem);
            return newItem;
        }
    }

    @Override
    public void clearCart(String cartKey) {
        cartRedisDao.clear();
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        return cartRedisDao.getCartItem(skuId.toString());
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        final Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.userInfoThreadLocal.get();
        if (userInfoTo.getUserId() != null) {
            final List<CartItem> tempItems = cartRedisDao.getTempCardItems();
            if (tempItems != null) {
                for (CartItem tempCartItem : tempItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                cartRedisDao.clearTemp();
            }
            cart.setItems(cartRedisDao.getAllItems());
        } else {
            List<CartItem> cartItems = cartRedisDao.getAllItems();
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        final CartItem cartItem1 = cartRedisDao.getCartItem(skuId.toString());
        cartItem1.setCheck(check == 1);
        cartRedisDao.setCartItem(skuId.toString(), cartItem1);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        final CartItem cartItem1 = cartRedisDao.getCartItem(skuId.toString());
        cartItem1.setCount(num);
        cartRedisDao.setCartItem(skuId.toString(), cartItem1);
    }

    @Override
    public void deleteItem(Long skuId) {
        cartRedisDao.remoteCartItem(skuId.toString());
    }

    @Override
    public List<CartItem> currentUserCartCheckedItems() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoThreadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            return cartRedisDao.getAllItems().stream()
                .filter(CartItem::getCheck)
                .peek((cartItem) -> {
                    R r = productFeignService.getPrice(cartItem.getSkuId());
                    cartItem.setPrice(r.getData(new TypeReference<BigDecimal>() {}));
                })
                .collect(Collectors.toList());
        }

    }

}
