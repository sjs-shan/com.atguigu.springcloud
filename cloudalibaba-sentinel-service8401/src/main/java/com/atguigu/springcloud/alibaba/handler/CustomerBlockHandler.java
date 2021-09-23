package com.atguigu.springcloud.alibaba.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;

public class CustomerBlockHandler {

    public static CommonResult handlerException1(BlockException exception){
        return new CommonResult(444,"....CustomerBlockHandler11111111");
    }
    public static CommonResult handlerException2(BlockException exception){
        return new CommonResult(2020,"....CustomerBlockHandler222222222");
    }
}
