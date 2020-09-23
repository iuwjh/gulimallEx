package com.atguigu.gulimall.order.dao.amqp;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRabbitSender {

    private final RabbitTemplate rabbitTemplate;

    public void createOrder(OrderEntity newOrder) {
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", newOrder);
    }

    public void closeOrder(OrderTo orderToClose) {
        rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderToClose);
    }
}
