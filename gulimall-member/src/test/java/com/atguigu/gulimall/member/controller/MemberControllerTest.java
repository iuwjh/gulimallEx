package com.atguigu.gulimall.member.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.config.GulimallProps;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.config.MemberWebConfig;
import com.atguigu.gulimall.member.controller.vo.MemberRegistVo;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.atguigu.common.Rmatcher.Rm;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(
    controllers = MemberController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MemberWebConfig.class),
    properties = {"logging.level.ROOT=debug"}
)
@EnableConfigurationProperties(GulimallProps.class)
@ActiveProfiles("testWithData")
class MemberControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper mapper) {
            return new MappingJackson2HttpMessageConverter(mapper);
        }

        @Bean
        public ObjectMapper objectMapper() {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
    }

    @MockBean
    MemberService memberService;

    @MockBean
    CouponFeignService couponFeignService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/member/member";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void coupons() throws Exception {
        final List<Object> coupons = Collections.singletonList(new SocialUserAccessVo().setUid("test"));
        Mockito.when(couponFeignService.membercoupons())
            .thenReturn(R.ok().put("coupons", coupons));

        mockMvc.perform(get(BASE_URL + "/coupons"))
            .andExpect(Rm().hasKeyAndJsonValue("coupons", coupons));
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
            .andExpect(Rm().hasJsonData(expected, new TypeReference<MemberEntity>() {}));
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
            .andExpect(Rm().hasJsonData(expected, new TypeReference<MemberEntity>() {}));
    }

    @Test
    void regist() throws Exception {
        final MemberRegistVo registVo = new MemberRegistVo();

        mockMvc.perform(post(BASE_URL + "/regist")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registVo)))
            .andExpect(Rm().hasStatus(200));
    }

    @Test
    void list() throws Exception {
        final Map<String, Object> params = new HashMap<>();
        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        final PageUtils page = new PageUtils(list, 5, 5, 1);
        Mockito.when(memberService.queryPage(params))
            .thenReturn(page);

        final LinkedMultiValueMap<String, String> reqParams = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            reqParams.add(e.getKey(), e.getValue().toString());
        }
        mockMvc.perform(post(BASE_URL + "/list")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .params(reqParams))
            .andExpect(Rm().hasKeyAndJsonValue("page", page));
    }

    @Test
    void info() throws Exception {
        final long id = 1L;
        final MemberEntity expected = new MemberEntity();
        Mockito.when(memberService.getById(1L))
            .thenReturn(expected);

        mockMvc.perform(get(BASE_URL + "/info/{id}", id))
            .andExpect(Rm().hasKeyAndJsonValue("member", expected));
    }

    @Test
    void save() throws Exception {
        final MemberEntity entity = new MemberEntity().setNickname("test");
        Mockito.when(memberService.save(entity))
            .thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entity)))
            .andExpect(Rm().hasStatus(200));
    }

    @Test
    void update() throws Exception {
        final MemberEntity entity = new MemberEntity().setNickname("test");
        Mockito.when(memberService.updateById(entity))
            .thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(entity)))
            .andExpect(Rm().hasStatus(200));
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
            .andExpect(Rm().hasStatus(200));
    }
}
