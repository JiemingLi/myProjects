package com.jieming.miaosha.controller;

import com.jieming.miaosha.access.AccessLimit;
import com.jieming.miaosha.domain.MiaoshaOrder;
import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.domain.OrderInfo;
import com.jieming.miaosha.rabbitmq.MQSender;
import com.jieming.miaosha.rabbitmq.MiaoshaMessage;
import com.jieming.miaosha.redis.GoodsKey;
import com.jieming.miaosha.redis.MiaoshaKey;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.result.CodeMsg;
import com.jieming.miaosha.result.Result;
import com.jieming.miaosha.service.GoodsService;
import com.jieming.miaosha.service.MiaoshaService;
import com.jieming.miaosha.service.OrderService;
import com.jieming.miaosha.util.MD5Util;
import com.jieming.miaosha.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private MQSender mqSender;

    //用来标记某个秒杀商品
    private Map<Long,Boolean> localOverMap = new HashMap<>();

    //初始化bean所回调的方法
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        for (GoodsVo goodVo: goodsVoList
             ) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goodVo.getId(),goodVo.getStockCount());
            localOverMap.put(goodVo.getId(),false);
        }
    }


    @RequestMapping(value = "/{path}/do_miaosha",method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(MiaoshaUser user, Model model,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //校验路径参数是否正确
        boolean check = miaoshaService.check(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        boolean isOver = localOverMap.get(goodsId);
        if(isOver){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //预先减少库存，如果数据不够，就直接返回，不用直接查询数据库
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if(stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null){
            return Result.error(CodeMsg.REPEATED_MIAOSHA);
        }

        //秒杀请求入消息队列
        MiaoshaMessage message = new MiaoshaMessage();
        message.setUser(user);
        message.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(message);
        //返回排队中，非阻塞
        return Result.success(0);

//        //1.
//        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        //判断库存够不够,也就是小于0直接返回错误信息，如果有库存，但是野重复秒杀，也会返回错误信息
//        Integer stock = goods.getStockCount();
//        if(stock <= 0){
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }else{
//            //2.
//            MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//            if(order != null){
//                return Result.error(CodeMsg.REPEATED_MIAOSHA);
//            }
//        }
//        //减库存，下订单，写入秒杀订单，原子性操作
//        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//        return Result.success(orderInfo);
    }

    //轮询访问缓存是否秒杀成功
    @AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> mresult(MiaoshaUser user,@Param("goodsId") long goodsId){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user, goodsId);
        return Result.success(result);
    }


    @AccessLimit(seconds = 10,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(MiaoshaUser user,@Param("goodsId") long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        boolean res = miaoshaService.chechVerifyCode(user, goodsId, verifyCode);

        if(!res){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //随机生成的path，并且保存到缓存当中
        String path = miaoshaService.createPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

}
