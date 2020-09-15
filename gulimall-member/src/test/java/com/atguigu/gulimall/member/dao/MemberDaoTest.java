package com.atguigu.gulimall.member.dao;

import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class MemberDaoTest {
    @Autowired
    MemberDao memberDao;

    @BeforeEach
    public void setup() {
        memberDao.delete(null);
        memberDao.insert(createEntity());
    }

    @Test
    public void select() {
        MemberEntity entity = memberDao.selectOne(null);
        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isNotNull().isEqualTo("test-user");
    }

    @Test
    public void update() {
        MemberEntity entity = memberDao.selectOne(null);
        entity.setUsername("abc");
        memberDao.updateById(entity);
        entity = memberDao.selectOne(null);
        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isNotNull().isEqualTo("abc");
    }

    @Test
    public void delete() {
        List<MemberEntity> entities = memberDao.selectList(null);
        assertThat(entities).hasSize(1);
        memberDao.delete(null);
        entities = memberDao.selectList(null);
        assertThat(entities).hasSize(0);
    }

    @Test
    public void insert() {
        memberDao.delete(null);
        MemberEntity entity = new MemberEntity().setUsername("test-name");

        memberDao.insert(entity);
        List<MemberEntity> entities = memberDao.selectList(null);
        assertThat(entities).hasSize(1);

        MemberEntity persistedEntity = entities.get(0);
        assertThat(persistedEntity.getId()).isNotNull();
        assertThat(persistedEntity).isNotNull().isEqualTo(entity);
    }

    private MemberEntity createEntity() {
        return new MemberEntity().setUsername("test-user");
    }
}
