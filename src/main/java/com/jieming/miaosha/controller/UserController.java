package com.jieming.miaosha.controller;

import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> getInfo(Model model, MiaoshaUser user){
        return Result.success(user);
    }

}
