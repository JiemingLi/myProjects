package com.jieming.miaosha.redis;

public class MiaoshaUserKey extends BasePrefix {

    private final static int TOKEN_EXPIRE = 3600*24*2;


    private MiaoshaUserKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE,"tk");
    public static MiaoshaUserKey getById = new MiaoshaUserKey(0,"id");




}
