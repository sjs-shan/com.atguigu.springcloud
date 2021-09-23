package com.atguigu.springcloud.alibaba.dao;

import com.atguigu.springcloud.alibaba.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderDao {

    //创建订单
    void create(Order order);

    //更新订单状态 从0改为1
    void update(@Param("userId") Long userId,@Param("status") Integer status);
}
