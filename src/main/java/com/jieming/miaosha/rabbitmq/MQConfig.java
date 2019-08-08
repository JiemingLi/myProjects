package com.jieming.miaosha.rabbitmq;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";

    public static final String MIAOSHA_QUEUE = "queue-7";

    public static final String EXCHANGE_NAME = "exchange-7";

    public static final String ROUTING_KEY = "lijieming.*";

    public static final String DEADEXCHANGE_NAME = "deadTopicExchange";

    public static final String DEADQUEUE_NAME = "deadQueue";



    @Bean
    public Queue queue(){
        return new Queue(QUEUE,true);
    }

    @Bean
    public Queue miaoshaQueue(){
        return new Queue(MIAOSHA_QUEUE,true);
    }

    @Bean
    public Queue deadQueue(){
        return new Queue(DEADQUEUE_NAME,true);
    }

    @Bean
    public TopicExchange deadExchange(){
        return new TopicExchange(DEADEXCHANGE_NAME,true,false);
    }

    @Bean
    public Binding deadBinding(){
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(ROUTING_KEY);
    }

}
