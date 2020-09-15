package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.dao.MemberReceiveAddressDao;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;
import com.atguigu.gulimall.member.service.impl.MemberReceiveAddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MemberReceiveAddressServiceTest {
    @Mock
    private MemberReceiveAddressDao memberReceiveAddressDao;

    private MemberReceiveAddressService memberReceiveAddressService;

    @BeforeEach
    void setup() {
        this.memberReceiveAddressService = Mockito.spy(new MemberReceiveAddressServiceImpl(memberReceiveAddressDao));
    }

    @Test
    void getAddressShouldReturnMemberReceiveAddresses() {
        final long memberId = 2L;
        Mockito.when(memberReceiveAddressDao.listByMemberId(memberId))
            .thenReturn(Collections.singletonList(new MemberReceiveAddressEntity()));

        assertThat(memberReceiveAddressService.getAddress(memberId)).hasSize(1);
    }
}
