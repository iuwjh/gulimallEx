package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handleOrderClose(SeckillOrderTo orderTo, Channel channel, Message message) throws IOException {
        System.out.println("MQ: 创建秒杀单 "+orderTo);
        try {
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
