package com.jieming.miaosha.redis;

/*运用模板设计模式*/
public interface KeyPrefix {

    int expireSeconds();

    String getPrefix();
}
