package com.atguigu.gulimall.order.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Lazy
    @Autowired
    RabbitTemplate rabbitTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void doInitDummyConnection() {
        rabbitTemplate.getConnectionFactory().createConnection().close();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // @PostConstruct
    private void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
        });

        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
        });
    }
}
