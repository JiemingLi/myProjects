package com.jieming.rabbitmq.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MyFactory {

    private static ConnectionFactory factory = new ConnectionFactory();

    public static Channel getChannel() throws Exception{
        factory.setHost("101.132.243.102");
        factory.setPort(5672);
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }
}
