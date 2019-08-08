package com.jieming.miaosha.controller;
import com.jieming.miaosha.result.Result;
import com.jieming.miaosha.service.MiaoshaUserService;
import com.jieming.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    MiaoshaUserService userService;


    private static Logger logger = LoggerFactory.getLogger(LoginController.class);


    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @ResponseBody
    @PostMapping("/do_login")
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        logger.info(loginVo.toString());
        String res = userService.login(response,loginVo);
        return  Result.success(res);
    }
}
