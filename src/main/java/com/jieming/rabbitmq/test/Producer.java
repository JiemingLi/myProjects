package com.jieming.rabbitmq.test;

import com.jieming.rabbitmq.util.MyFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.ReturnListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


public class Producer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) throws Exception {
        Channel channel = MyFactory.getChannel();
        //指定消息的接受模式
        channel.confirmSelect();

        //发送消息
        String exchangeName = "test_dlx2_exchange";
        String routingKey = "dlx2_save";
        for (int i = 0; i < 1; i++) {
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2)
                    .contentEncoding("UTF-8")
                    .expiration("10000")
                    .build();
            String msg = "hello rabbitmq send Consumer message" + i;
            channel.basicPublish(exchangeName,routingKey,true,properties,msg.getBytes());
        }

        // 添加确认监听
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliverTag, boolean multiple) throws IOException {
                //deliverTag就是唯一的发送的消息的标签，用来表示消息，返回的时候会根据这个进行确认
                //消息到底有没有成功投递
                System.out.println("---ack---");
            }

            @Override
            public void handleNack(long deliverTag, boolean multiple) throws IOException {
                System.out.println("---No---ack---");
            }
        });

        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int i, String s, String s1, String s2, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                System.out.println("return");
            }
        });

    }
}
