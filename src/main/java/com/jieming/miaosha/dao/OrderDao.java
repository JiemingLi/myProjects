package com.jieming.miaosha.dao;

import org.apache.ibatis.annotations.*;

import com.jieming.miaosha.domain.MiaoshaOrder;
import com.jieming.miaosha.domain.OrderInfo;

@Mapper
public interface OrderDao {
	
	@Select("select * from miaosha_order where user_id=#{userId} and goods_id=#{goodsId}")
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

	@Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, order_status, create_date)values("
			+ "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} ) ")
//	@SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    @Options(useGeneratedKeys = true,keyProperty = "id")
	public long insert(OrderInfo orderInfo);
	
	@Insert("insert into miaosha_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
	public int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);


	@Select("select * from order_info where id = #{orderId}")
    @Results({
            @Result(column = "order_status",property = "status")
    })
    OrderInfo getOrderById(@Param("orderId") long orderId);
}
