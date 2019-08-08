package com.jieming.miaosha.dao;

import com.jieming.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.*;


@Mapper
public interface MiaoshaUserDao {
    @Select("select * from miaosha_user where id = #{id}")
    @Results(value = {
            @Result(column = "pwd",property = "password")
    })
    MiaoshaUser getById(long id);

    @Update("update miaosha_user set password = #{password} where id = #{id}")
    void update(MiaoshaUser toUpdateUser);
}
