package com.atguigu.gulimall.cart.service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.dao.CartRedisDao;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.impl.CartServiceImpl;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.SkuSaleAttrValueTo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock
    private ProductFeignService productFeignService;

    @Mock
    private CartRedisDao cartRedisDao;

    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    private CartService cartService;

    @BeforeEach
    void setup() {
        cartService = Mockito.spy(new CartServiceImpl(productFeignService, cartRedisDao, executor));
    }

    @Test
    void addToCartOldItem() throws Exception {
        final long skuId = 2L;
        final int num = 5;
        final CartItem cartItem = new CartItem().setCount(5);
        Mockito.when(cartRedisDao.getCartItem(String.valueOf(skuId))).thenReturn(cartItem);

        final CartItem result = cartService.addToCart(skuId, num);
        assertThat(result.getCount()).isNotNull().isEqualTo(num + 5);
    }

    @Test
    void addToCartNewItem() throws Exception {
        final long skuId = 2L;
        final int num = 5;
        final String skuTitle = "abc";
        Mockito.when(cartRedisDao.getCartItem(anyString())).thenReturn(null);
        Mockito.when(productFeignService.skuInfo(skuId))
            .thenReturn(R.ok().put("skuInfo", new SkuInfoVo().setSkuTitle(skuTitle)));
        Mockito.when(productFeignService.infoBySkuId(skuId))
            .thenReturn(singletonList(new SkuSaleAttrValueTo().setAttrName("attr_1").setAttrValue("val_1")));

        final CartItem result = cartService.addToCart(skuId, num);
        assertThat(result.getTitle()).isNotNull().isEqualTo(skuTitle);
        assertThat(result.getSkuAttr()).isNotNull().hasSize(1);
    }

    @Test
    void getCartNotLoggedIn() throws Exception {
        CartInterceptor.userInfoThreadLocal.set(new UserInfoTo().setUserId(null));

        cartService.getCart();
        Mockito.verify(cartRedisDao, Mockito.times(1)).getAllItems();
    }

    @Test
    void getCartLoggedIn() throws Exception {
        final CartItem cartItem = new CartItem().setSkuId(4L).setCount(5);
        CartInterceptor.userInfoThreadLocal.set(new UserInfoTo().setUserId(3L));
        Mockito.when(cartRedisDao.getTempCardItems()).thenReturn(singletonList(cartItem));
        Mockito.when(productFeignService.skuInfo(any()))
            .thenReturn(R.ok().put("skuInfo", new SkuInfoVo()));

        cartService.getCart();
        Mockito.verify(cartService, Mockito.times(1))
            .addToCart(cartItem.getSkuId(), cartItem.getCount());
    }

    @Test
    void currentUserCartItemsNotLoggedIn() throws Exception {
        CartInterceptor.userInfoThreadLocal.set(new UserInfoTo());

        assertThat(cartService.currentUserCartItems()).isNull();
    }

    @Test
    void currentUserCartItemsLoggedIn() throws Exception {
        final long skuId = 3L;
        final BigDecimal price = new BigDecimal(300);
        final CartItem cartItem = new CartItem().setSkuId(skuId);
        CartInterceptor.userInfoThreadLocal.set(new UserInfoTo().setUserId(6L));
        Mockito.when(cartRedisDao.getAllItems()).thenReturn(singletonList(cartItem));
        Mockito.when(productFeignService.getPrice(skuId)).thenReturn(R.ok().setData(price));

        final List<CartItem> result = cartService.currentUserCartItems();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isNotNull().isEqualTo(price);
    }

}
