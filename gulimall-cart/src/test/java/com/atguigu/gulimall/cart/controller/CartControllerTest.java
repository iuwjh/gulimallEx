package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.controller.ControllerTestConfig;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;

import static com.atguigu.common.Rmatcher.Rm;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = CartController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class CartControllerTest {
    @MockBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "";

    @Test
    void cartListPage() throws Exception {
        final Cart cart = new Cart();
        final CartItem cartItem = new CartItem().setCheck(true).setPrice(new BigDecimal(200)).setCount(2);
        cart.setItems(singletonList(cartItem));
        Mockito.when(cartService.getCart()).thenReturn(cart);

        mockMvc.perform(get(BASE_URL + "/cart.html"))
            .andExpect(MockMvcResultMatchers.model().attribute("cart", Matchers.notNullValue()))
            .andExpect(MockMvcResultMatchers.view().name("cartList"));
    }

    @Test
    void addToCart() throws Exception {
        Mockito.when(cartService.getCart()).thenReturn(new Cart());

        final LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("skuId", "2");
        mockMvc.perform(get(BASE_URL + "/addToCart")
            .queryParam("skuId", "2")
            .queryParam("num", "3"))
            .andExpect(MockMvcResultMatchers.model().attribute("skuId", Matchers.notNullValue()))
            .andExpect(Rm().redirectedToWithQueryParams("http://cart.gulimall.com/addToCartSuccess.html", queryParams));
    }

    @Test
    void addToCartSuccess() throws Exception {
        final long skuId = 2L;
        Mockito.when(cartService.getCartItem(skuId)).thenReturn(new CartItem());

        mockMvc.perform(get(BASE_URL + "/addToCartSuccess.html")
            .queryParam("skuId", "2"))
            .andExpect(MockMvcResultMatchers.model().attribute("item", Matchers.notNullValue()))
            .andExpect(MockMvcResultMatchers.view().name("success"));
    }
}
