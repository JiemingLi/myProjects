package com.jieming.miaosha.controller;

import com.jieming.miaosha.domain.User;
import com.jieming.miaosha.rabbitmq.MQSender;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.redis.UserKey;
import com.jieming.miaosha.result.Result;
import com.jieming.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SampleController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name","jiemingli");
        return "hello";
    }

    @RequestMapping("/do/get")
    @ResponseBody
    public Result<User> doGet(Model model){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/do/tx")
    @ResponseBody
    public Result<Boolean> doTx(){
        boolean res = userService.tx();
        return Result.success(res);
    }

    @RequestMapping("/do/redis")
    @ResponseBody
    public Result<Boolean> doRedis(){
        boolean res = userService.tx();
        return Result.success(res);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User res = redisService.get(UserKey.getById,""+1,User.class);
        return Result.success(res);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User(1,"1111");
        boolean res = redisService.set(UserKey.getById,""+1,user);
        return Result.success(res);
    }


    @Autowired
    private MQSender mqSender;

//    @RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mqTest(){
//       mqSender.send("jiemingli's mq");
//        return Result.success("jiemingli's mq success");
//    }





}
