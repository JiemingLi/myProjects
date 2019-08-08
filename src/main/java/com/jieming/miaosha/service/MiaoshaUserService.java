package com.jieming.miaosha.service;

import com.jieming.miaosha.dao.MiaoshaUserDao;
import com.jieming.miaosha.domain.MiaoshaUser;
import com.jieming.miaosha.exception.GlobalException;
import com.jieming.miaosha.redis.MiaoshaUserKey;
import com.jieming.miaosha.redis.RedisService;
import com.jieming.miaosha.result.CodeMsg;
import com.jieming.miaosha.util.MD5Util;
import com.jieming.miaosha.util.UUIDUtil;
import com.jieming.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public final static String COOKI_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    private RedisService redisService;

    public MiaoshaUser getById(long id ){
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if(user!=null){
            System.out.println("user from cache...");
            return user;
        }
        user = miaoshaUserDao.getById(id);
        redisService.set(MiaoshaUserKey.getById,""+id,user);
        return user;
    }


    //更新密码
    public boolean updatePassword(long id,String formPass,String token){
        //从缓存中取出user对象
        MiaoshaUser user = getById(id);
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        //更新数据库
        MiaoshaUser toUpdateUser = new MiaoshaUser();
        toUpdateUser.setId(id);
        toUpdateUser.setPassword(MD5Util.formPassToDB(formPass,user.getSalt()));
        miaoshaUserDao.update(toUpdateUser);

        //处理缓存
        //删除用户缓存
        redisService.delete(MiaoshaUserKey.getById,""+id);
        //重新设置,让它修改完密码之后还可以继续访问，但是重新登录的话就要修改密码了！
        user.setPassword(toUpdateUser.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);
        return true;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {

        if(loginVo == null){
            throw new GlobalException(CodeMsg.CONTENT_NULL);
        }

        MiaoshaUser miaoshaUser = getById(Long.parseLong(loginVo.getMobile()));
        //查询手机号
        if(miaoshaUser == null){
//            return CodeMsg.MOBILE_NOT_EXIST;
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        //验证密码
        String dbPass = miaoshaUser.getPassword();
        String saltDB = miaoshaUser.getSalt();

        String calPass = MD5Util.formPassToDB(loginVo.getPassword(),saltDB);
        if(!calPass.equals(dbPass)){
//            return CodeMsg.PASSWORD_ERROR;
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成，添加到cookie
        String token  = null;
        token = UUIDUtil.uuid();
        addCookie(miaoshaUser,token,response);
        return token;

    }

    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)) return null;
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延长有效期，重新把缓存值设置，然后再写回到cookie
        if(user!=null){
            addCookie(user,token,response);
        }
        return user;
    }

    private void addCookie(MiaoshaUser user,String token,HttpServletResponse response){
        //标识哪个用户对应哪个token,用户信息写入到redis中
        redisService.set(MiaoshaUserKey.token,token,user);
        //将token返回给客户端
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
