package com.jieming.miaosha.service;

import java.util.Date;

import com.jieming.miaosha.redis.OrderKey;
import com.jieming.miaosha.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jieming.miaosha.dao.OrderDao;
import com.jieming.miaosha.domain.MiaoshaOrder;
import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.domain.OrderInfo;
import com.jieming.miaosha.vo.GoodsVo;

@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;

	@Autowired
	private RedisService redisService;


	//每次都是直接从缓存取
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
//		return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		return redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class);
	}

	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
		//添加订单信息
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());//订单创建日期
		orderInfo.setDeliveryAddrId(0L);//
		orderInfo.setGoodsCount(1);//购买商品的数量
		orderInfo.setGoodsId(goods.getId());//商品的ID
		orderInfo.setGoodsName(goods.getGoodsName());//商品的名字
		orderInfo.setGoodsPrice(goods.getMiaoshaPrice());//购买的时候的价格
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);//订单的状态
		orderInfo.setUserId(user.getId());//哪个用户购买
		long orderId = orderDao.insert(orderInfo);


		//添加秒杀订单信息
		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goods.getId());//设置秒杀订单的商品ID
		miaoshaOrder.setOrderId(orderInfo.getId());//说明是属于哪一个订单的id
		miaoshaOrder.setUserId(user.getId());//属于哪个用户
		orderDao.insertMiaoshaOrder(miaoshaOrder);

		redisService.set(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goods.getId(),miaoshaOrder);

		return orderInfo;
	}

	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}
}
