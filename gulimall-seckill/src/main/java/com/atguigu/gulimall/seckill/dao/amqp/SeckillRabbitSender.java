package com.atguigu.gulimall.seckill.dao.amqp;

import com.atguigu.common.to.SeckillOrderTo;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SeckillRabbitSender {

    private final RabbitTemplate rabbitTemplate;



    public void createOrder(SeckillOrderTo orderTo) {
        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
    }

}
