package com.jieming.miaosha.controller;


import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.domain.OrderInfo;
import com.jieming.miaosha.result.CodeMsg;
import com.jieming.miaosha.result.Result;
import com.jieming.miaosha.service.GoodsService;
import com.jieming.miaosha.service.OrderService;
import com.jieming.miaosha.vo.GoodsVo;
import com.jieming.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> to_detail(Model model, MiaoshaUser miaoshaUser, MiaoshaUser user, @RequestParam("orderId") long orderId){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderById(orderId);
        if(order==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoods(goods);
        orderDetailVo.setOrder(order);
        return Result.success(orderDetailVo);
    }
}
