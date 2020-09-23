package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisPlusTest(properties = {"spring.liquibase.change-log=classpath:/liquibase/master.xml"})
public class OrderDaoTest {
    @Autowired
    OrderDao orderDao;


    @BeforeEach
    public void setup() {
        orderDao.delete(null);
        orderDao.insert(createEntity());
    }

    private OrderEntity createEntity() {
        return new OrderEntity();
    }
}
