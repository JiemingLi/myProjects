package com.jieming.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(Object msg, Map<String,Object> properties) throws Exception{
        MessageHeaders messageHeaders = new MessageHeaders(properties);
        Message message  = MessageBuilder.createMessage(msg,messageHeaders);
        rabbitTemplate.convertAndSend("exchange-1","springboot.hello",message);
    }
}

