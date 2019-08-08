package com.jieming.miaosha.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//多少s之内访问多少次
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {
    int seconds(); // 多少秒
    int maxCount();// 多少次
    boolean needLogin() default true; //默认需要登陆
}
