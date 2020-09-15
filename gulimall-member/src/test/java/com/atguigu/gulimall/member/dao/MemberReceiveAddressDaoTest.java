package com.atguigu.gulimall.member.dao;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class MemberReceiveAddressDaoTest {
    @Autowired
    MemberReceiveAddressDao memberReceiveAddressDao;

    @Test
    void listByMemberIdShouldReturn() {
        assertThat(memberReceiveAddressDao.listByMemberId(7L)).hasSize(3);
    }
}
