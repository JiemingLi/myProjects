package com.jieming.miaosha.redis;

import com.alibaba.fastjson.JSON;
import com.jieming.miaosha.domain.MiaoshaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

@Service
public class RedisService {


    @Autowired
    JedisPool jedisPool;

    /*通过key获取值*/
    public <T> T get(KeyPrefix prefix , String key,Class<T> clazz){
        Jedis jedis  = null;
            try{
                jedis = jedisPool.getResource();
                String realKey = prefix.getPrefix() + key;
                String str = jedis.get(realKey);
                T t = stringToBean(str,clazz);
                return  t;
        }finally {
            returnToPool(jedis);
        }
    }

    /*设置值*/
    public <T> boolean set(KeyPrefix prefix ,String key,T value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realValue = beanToString(value);
            if(realValue == null || realValue.length() == 0){
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();

            if( seconds <= 0){
                /*没有设置过期时间的情况*/
                jedis.set(realKey,realValue);
            }else{
                /*设置了过期时间的情况*/
                jedis.setex(realKey,seconds,realValue);
            }

            return  true;
        }finally {
            returnToPool(jedis);
        }
    }


    /*对传入的key对应的值进行自增*/
    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis = null;

        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            Long incr = jedis.incr(realKey);
            return incr;
        }finally {
            returnToPool(jedis);
        }
    }

    /*对传入的key对应的值进行自减*/
    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis = null;

        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            /*院子操作，即自减的时候要么一起执行，要么不执行，同理自增也是一样
            * 就是不会有中间状态
            * */
            Long decr = jedis.decr(realKey);
            return decr;
        }finally {
            returnToPool(jedis);
        }
    }



    /*判断key是否存在*/
    public <T> boolean exits(KeyPrefix prefix,String key){
        Jedis jedis = null;
        Boolean exists = false;
        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;

            exists = jedis.exists(realKey);
            return exists;
        }finally {
            returnToPool(jedis);
        }
    }

    public static <T> String beanToString(T value) {
        if(value == null){
            return null;
        }
        /*获取bean的类型*/
        Class<?> clazz = value.getClass();

        /*判断是什么类型，这里都是转化为string*/
        if(clazz == int.class || clazz == Integer.class){
            return "" + value;
        }

        if(clazz == int.class || clazz == Integer.class) {
            return ""+value;
        }else if(clazz == String.class) {
            return (String)value;
        }else if(clazz == long.class || clazz == Long.class) {
            return ""+value;
        }else {
            return JSON.toJSONString(value);
        }
    }


    /*在redis查询出来的都是String类型，那么要看get的时候需要转为什么类型*/
    public static <T> T stringToBean(String str,Class<T> clazz) {
        if(str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if(clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        }else if(clazz == String.class) {
            return (T)str;
        }else if(clazz == long.class || clazz == Long.class) {
            return  (T)Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }


    /*将jedis返回到池中*/
    private void returnToPool(Jedis jedis) {
        if(jedis != null){
            jedis.close();
        }
    }

    /**
     * 删除
     * */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            long ret =  jedis.del(realKey);
            return ret > 0;
        }finally {
            returnToPool(jedis);
        }
    }



}
