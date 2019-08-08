package com.jieming.miaosha.access;

import com.jieming.miaosha.domain.MiaoshaUser;

public class UserContext {
    private static ThreadLocal<MiaoshaUser> threadLocal = new ThreadLocal<>();

    public static void setMiaoshaUser(MiaoshaUser user){
        threadLocal.set(user);
    }

    public static MiaoshaUser getMiaoshaUser(){
        return threadLocal.get();
    }
}
