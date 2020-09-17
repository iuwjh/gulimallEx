package com.atguigu.gulimall.member.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.CommonTestHelper;
import com.atguigu.common.ControllerTestConfig;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.controller.vo.MemberRegistVo;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static com.atguigu.common.Rmatcher.Rm;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = MemberController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
class MemberControllerTest {

    @MockBean
    private MemberService memberService;

    @MockBean
    private CouponFeignService couponFeignService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/member/member";

    @Test
    void coupons() throws Exception {
        final List<Object> coupons = Collections.singletonList(new SocialUserAccessVo().setUid("test"));
        Mockito.when(couponFeignService.membercoupons())
            .thenReturn(R.ok().put("coupons", coupons));

        mockMvc.perform(get(BASE_URL + "/coupons"))
            .andExpect(Rm().RHasAttr("coupons", coupons));
    }

    @Test
    void oauthLogin() throws Exception {
        final SocialUserAccessVo accessVo = new SocialUserAccessVo().setUid("test");
        final MemberEntity expected = new MemberEntity().setSocialUid("test");
        Mockito.when(memberService.loginSocial(accessVo))
            .thenReturn(expected);

        mockMvc.perform(post(BASE_URL + "/oauth2/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accessVo)))
            .andExpect(Rm().RDataEquals(expected, new TypeReference<MemberEntity>() {}));
    }

    @Test
    void login() throws Exception {
        final MemberLoginVo loginVo = new MemberLoginVo().setPassword("test");
        final MemberEntity expected = new MemberEntity().setPassword("test");
        Mockito.when(memberService.login(loginVo))
            .thenReturn(expected);

        mockMvc.perform(post(BASE_URL + "/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginVo)))
            .andExpect(Rm().RDataEquals(expected, new TypeReference<MemberEntity>() {}));
    }

    @Test
    void regist() throws Exception {
        final MemberRegistVo registVo = new MemberRegistVo();

        mockMvc.perform(post(BASE_URL + "/regist")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registVo)))
            .andExpect(Rm().statusEquals(200));
    }

    @Test
    void list() throws Exception {
        final Map<String, Object> params = new HashMap<>();
        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        final PageUtils page = new PageUtils(list, 5, 5, 1);
        Mockito.when(memberService.queryPage(params))
            .thenReturn(page);

        mockMvc.perform(post(BASE_URL + "/list")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .params(CommonTestHelper.mapToMultiValueMap(params)))
            .andExpect(Rm().RHasAttr("page", page));
    }

    @Test
    void info() throws Exception {
        final long id = 1L;
        final MemberEntity expected = new MemberEntity();
        Mockito.when(memberService.getById(1L))
            .thenReturn(expected);

        mockMvc.perform(get(BASE_URL + "/info/{id}", id))
            .andExpect(Rm().RHasAttr("member", expected));
    }

    @Test
    void save() throws Exception {
        final MemberEntity entity = new MemberEntity().setNickname("test");
        Mockito.when(memberService.save(entity))
            .thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entity)))
            .andExpect(Rm().statusEquals(200));
    }

    @Test
    void update() throws Exception {
        final MemberEntity entity = new MemberEntity().setNickname("test");
        Mockito.when(memberService.updateById(entity))
            .thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entity)))
            .andExpect(Rm().statusEquals(200));
    }

    @Test
    void delete() throws Exception {
        final long[] ids = {1, 2, 3, 4, 5};
        final MemberEntity entity = new MemberEntity().setNickname("test");
        Mockito.when(memberService.removeByIds(Arrays.asList(ids)))
            .thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ids)))
            .andExpect(Rm().statusEquals(200));
    }
}
