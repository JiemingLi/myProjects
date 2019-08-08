package com.jieming.miaosha.redis;

public class OrderKey extends BasePrefix {
    public OrderKey(int expireSeconds,String prefix) {
        super( expireSeconds,prefix);
    }

    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey(60,"moug");
}
