package com.jieming.miaosha.redis;

public abstract  class BasePrefix implements KeyPrefix {

    /*过期时间*/
    private int expireSeconds;
    /*前缀*/
    private String prefix;

    /*
    * 表示不过期
    * */
    public BasePrefix( String prefix){
        this(0,prefix);
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        return getClass().getSimpleName() + ":" + prefix;
    }
}
