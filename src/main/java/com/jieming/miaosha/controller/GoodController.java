package com.jieming.miaosha.controller;

import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.redis.GoodsKey;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.result.Result;
import com.jieming.miaosha.service.GoodsService;
import com.jieming.miaosha.service.MiaoshaUserService;
import com.jieming.miaosha.vo.GoodsDetailVo;
import com.jieming.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodController {

    @Autowired
    private MiaoshaUserService miaoshaUserService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisService redisService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;


    //对商品的所有列表进行缓存 60s
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(MiaoshaUser user, Model model, HttpServletRequest request, HttpServletResponse response){
        model.addAttribute("user",user);
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if(html != null){
            return html;
        }
        List<GoodsVo> goodslist = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodslist);
        SpringWebContext ctx = new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
        //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goodsLiist", ctx);

        if(html!=null){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String datail(MiaoshaUser user,@PathVariable("goodsId") Long goodsId,Model model,HttpServletRequest request,HttpServletResponse response){

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if(html != null){
            return html;
        }

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        model.addAttribute("user",user);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshastatus = 0;
        int remainSeconds = 0;
        if(now < startAt){
            miaoshastatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        }else if(now > endAt){
            miaoshastatus = 0;
            remainSeconds = -1;
        }else{
            miaoshastatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("miaoshaStatus",miaoshastatus);
        model.addAttribute("remainSeconds",remainSeconds);
//        return "goods_detail";

        //手动渲染
        SpringWebContext ctx = new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if(html!=null) {
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;
    }


    //前后端分离，编写一个为前端服务的接口
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                                        @PathVariable("goodsId")long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        //GoodDetailVo 将数据封装，返回给前端
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }

}
