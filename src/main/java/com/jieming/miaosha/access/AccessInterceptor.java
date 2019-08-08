package com.jieming.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.redis.AccessKey;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.result.CodeMsg;
import com.jieming.miaosha.result.Result;
import com.jieming.miaosha.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private MiaoshaUserService miaoshaUserService;

    @Autowired
    private RedisService redisService;




    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(handler instanceof HandlerMethod){

            MiaoshaUser user = getUser(request,response);
            UserContext.setMiaoshaUser(user);
            HandlerMethod hm = (HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            //如果没有AccessLimit类型的注解，说明不需要验证,那么拦截器直接返回true
            if(accessLimit == null) {return true;}
            //获取注解的参数
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            //先定义key值，key值的全部内容就是 URI + user.getId()
            String key = request.getRequestURI();

            //如果需要登录，就进行验证cookie的token或者参数的token
            if(needLogin){
                if(user == null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_"+user.getId();
            }

            //指定缓存中设置注解上的时间
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if(count == null){
                redisService.set(ak,key,1);
            }else if(count < maxCount){
                redisService.incr(ak,key);
            }else{
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }


    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    public MiaoshaUser getUser(HttpServletRequest request,HttpServletResponse response){

        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValues(request,MiaoshaUserService.COOKI_NAME_TOKEN);

        String token = !StringUtils.isEmpty(paramToken)?paramToken:cookieToken;
        if(token == null) return null;
        MiaoshaUser user = miaoshaUserService.getByToken(response,token);
        return user;
    }

    private String getCookieValues(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;
        for (Cookie cookie: cookies
        ) {
            if(cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
