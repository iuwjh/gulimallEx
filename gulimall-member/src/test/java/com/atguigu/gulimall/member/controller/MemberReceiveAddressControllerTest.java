package com.atguigu.gulimall.member.controller;

import com.atguigu.common.ControllerTestConfig;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;
import com.atguigu.gulimall.member.service.MemberReceiveAddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static com.atguigu.common.Rmatcher.Rm;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = MemberReceiveAddressController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestConfig.class)
)
public class MemberReceiveAddressControllerTest {

    @MockBean
    private MemberReceiveAddressService memberReceiveAddressService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/member/memberreceiveaddress";

    @Test
    void getAddress() throws Exception {
        final long memberId = 2L;
        final MemberReceiveAddressEntity addressEntity = new MemberReceiveAddressEntity().setCity("Beijing");
        Mockito.when(memberReceiveAddressService.getAddress(memberId))
            .thenReturn(Collections.singletonList(addressEntity));

        mockMvc.perform(get(BASE_URL + "/{memberId}/address", memberId))
            .andExpect(Rm().contentAsCollHasSize(1, List.class));
    }
}
