package com.jieming.miaosha.rabbitmq;


import com.jieming.miaosha.domain.MiaoshaOrder;
import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.exception.GlobalException;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.result.CodeMsg;
import com.jieming.miaosha.service.GoodsService;
import com.jieming.miaosha.service.MiaoshaService;
import com.jieming.miaosha.service.OrderService;
import com.jieming.miaosha.vo.GoodsVo;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;


//    @Transactional
    @RabbitHandler
    @RabbitListener(
            bindings = {
                    @QueueBinding(
                            value = @Queue(value = "${spring.rabbitmq.listener.order.queue.name}",
                                    durable = "${spring.rabbitmq.listener.order.queue.durable}",
                                    arguments = {
                                            @Argument(name = "x-dead-letter-exchange",value = MQConfig.DEADEXCHANGE_NAME)
//                                            @Argument(name = "x-dead-letter-routing-key",value = "springjie")
                                    }
                            ),
                            exchange = @Exchange(value = "${spring.rabbitmq.listener.order.exchange.name}",
                                    durable = "${spring.rabbitmq.listener.order.exchange.durable}",
                                    type = "${spring.rabbitmq.listener.order.exchange.type}",
                                    ignoreDeclarationExceptions = "${spring.rabbitmq.listener.order.exchange.ignoreDeclarationExceptions}"),
                            key = "${spring.rabbitmq.listener.order.key}"
                    )
            }
    )
    public void receiver(Message message, Channel channel) throws Exception {
        try{
//            int i = 1 / 0;
            String msgg = new String(message.getBody(),"UTF-8");
            log.info("receive message:"+msgg);
            MiaoshaMessage msg = redisService.stringToBean(msgg, MiaoshaMessage.class);
            MiaoshaUser user = msg.getUser();
            long goodsId = msg.getGoodsId();
            //1. 在处理消息的时候判断此时还有没有数据，因为当获取到该消息，然后进行处理的时候，可能已经没有库存了。
            GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(msg.getGoodsId());
            int stock = goodsVo.getStockCount();
            if(stock <= 0 ){
                return;
            }
            //2. 判断是否已经秒杀过
            MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
            if(order != null) {
                return;
            }
            //3. 秒杀处理
            miaoshaService.miaosha(user,goodsVo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }

    }
}
