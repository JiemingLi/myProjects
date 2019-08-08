package com.jieming.miaosha.service;

import com.jieming.miaosha.domain.MiaoshaOrder;
import com.jieming.miaosha.redis.MiaoshaKey;
import com.jieming.miaosha.redis.MiaoshaUserKey;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.domain.OrderInfo;
import com.jieming.miaosha.vo.GoodsVo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;

	@Autowired
	private RedisService redisService;

	private static char[] ops = new char[] {'+', '-', '*'};

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		//3.
		boolean success = goodsService.reduceStock(goods);

		//order_info maiosha_order
		//4.
		if(success){
			return orderService.createOrder(user,goods);
		}else{
			this.setIsOver(goods.getId());
			return null;
		}
	}

	public long getMiaoshaResult(MiaoshaUser user,long goodsId){
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order!=null){
			return order.getOrderId();
		}else{
			if(getIsOver(goodsId)){
				return -1;
			}else{
				return 0;
			}
		}
	}

	private void setIsOver(long goodsId ){
		redisService.set(MiaoshaKey.isGoodsOver,goodsId+"",true);
	}

	private boolean getIsOver(long goodsId){
		return redisService.exits(MiaoshaKey.isGoodsOver,goodsId+"");
	}


	public boolean check(MiaoshaUser user,long goodsId,String path){
		if(user == null || path == null){
			return false;
		}
		String res = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);

		return res.equals(path);
	}

	public String createPath(MiaoshaUser user,long goodsId){
		if(user == null || goodsId < 0){
			return null;
		}
		//生成path
		String path = MD5Util.md5(UUID.randomUUID()+"123456");
		redisService.set(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId,path);
		return path;
	}


	private String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = ""+ num1 + op1 + num2 + op2 + num3;
		return exp;
	}

	public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码存到redis中
		int rnd = calc(verifyCode);
		redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
		//输出图片
		return image;
	}

	private int calc(String verifyCode) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer)engine.eval(verifyCode);
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}


	public boolean chechVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
		if(user == null || goodsId < 0){
			return false;
		}
		Integer code = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId() + "," + goodsId, Integer.class);
		if(code == null || code - verifyCode != 0){
			return false;
		}

		//删除验证码在redis的缓存
		redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
		return true;
	}
}
