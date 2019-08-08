package com.jieming.rabbitmq.test;

import com.jieming.rabbitmq.util.MyFactory;
import com.rabbitmq.client.*;
import org.springframework.amqp.rabbit.support.Delivery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
//消费端本来就是监听消息队列，就不用指定channel是什么模式了
public class Consumer {

    public static void main(String[] args) throws Exception {

        Channel channel = MyFactory.getChannel();

        //普通的交换器和队列和路由键
        String exchangeName = "test_dlx2_exchange";
        String routingKey = "dlx2_save";
        String queueName = "test_dlx2_queue";

        Map<String ,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","dlx2.exchange");
        //队列和exchange需要进行声明到mq上，相当于创建
        channel.exchangeDeclare(exchangeName,"topic",true,false,null);
        channel.queueDeclare(queueName,true,false,false,arguments);
        //绑定操作,声明消息队列和交换器，然后再和路由键一起绑定
        channel.queueBind(queueName,exchangeName,routingKey);


        //死信队列的声明和绑定
        channel.exchangeDeclare("dlx2.exchange","topic",true,false,null);
        channel.queueDeclare("dlx2.queue",true,false,false,null);
        channel.queueBind("dlx2.queue","dlx2.exchange","dlx2_save");



//        channel.basicQos(0,1,false);

        channel.basicConsume(queueName,true,new MyConsumer(channel));

    }
}
