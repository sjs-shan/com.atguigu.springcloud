package com.atguigu.springcloud.alibaba.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.springcloud.alibaba.handler.CustomerBlockHandler;
import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class FlowLimitController {

    @GetMapping("/testA")
    public String testA(){
        return "---------testA";
    }
    @GetMapping("/testB")
    public String testB() {

        return "------testB";
    }
    @GetMapping("/testD")
    public String testD() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("testD 测试RT");
        return "------testD";}

        @GetMapping("/testHotKey")
        @SentinelResource(value="testHotKey",blockHandler ="deal_testHotKey" )
        public String testHotKey(@RequestParam(value="p1",required =false) String p1
    ,@RequestParam(value="p2",required=false) String p2){
            return "----------testHotKey:{ OTO}";
        }

    //兜底方法
    public String deal_testHotKey(String p1, String p2, BlockException exception) {
        return "------deal_testHotKey,o(╥﹏╥)o";
    }

    @GetMapping("/byResource")
    @SentinelResource(value = "byResource", blockHandler = "handleException")
    public CommonResult<?> byResource() {
        return new CommonResult<Payment>(200, "按资源名称限流测试OK", new Payment(2020L, "serial001"));
    }

    public CommonResult<?> handleException(BlockException exception) {
        return new CommonResult<Payment>(444, exception.getClass().getCanonicalName() + "\t 服务不可用");
    }

    @GetMapping("/rateLimit/byUrl")
    @SentinelResource(value = "byUrl")
    public CommonResult<?> byUrl()
    {
        return new CommonResult<Payment>(200,"按url限流测试OK",new Payment(2020L,"serial002"));
    }

    @GetMapping("/rateLimit/customerBlockHandler")
    @SentinelResource(value="customerBlockHandler",
              blockHandlerClass = CustomerBlockHandler.class,
              blockHandler = "handlerException2")
    public CommonResult<Payment> customerBlockHandler() {
        return new CommonResult(200, "", new Payment(2020L, "serial003"));
    }





}
