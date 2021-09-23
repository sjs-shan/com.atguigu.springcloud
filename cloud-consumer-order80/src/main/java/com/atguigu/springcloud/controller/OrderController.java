package com.atguigu.springcloud.controller;

import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;
import com.netflix.discovery.DiscoveryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@Slf4j
public class OrderController {
    public static final String PAYMENT_URL="HTTP://CLOUD-PAYMENT-SERVICE";
//    public static final String PAYMENT_URL="http://localhost:8001";


    @Resource
   private RestTemplate restTemplate;

//    @Resource
//    private LoadBalancer loadBalancer;

    @Resource
    private DiscoveryClient discoveryClient;


    @PostMapping("/payment/create")
    public CommonResult<Payment> create(Payment payment){
        return restTemplate.postForObject(PAYMENT_URL+"/payment/create",payment,CommonResult.class);

    }
    @GetMapping( "/consumer/payment/get/{id}")
    public CommonResult<Payment> getPayment(@PathVariable("id") Long id){
        return restTemplate.getForObject(PAYMENT_URL+"/payment/get/"+id,CommonResult.class);

    }

//    public CommonResult<Payment> getPayment2(@PathVariable("id") Long id){
//        ResponseEntity<CommonResult> entity=restTemplate.getForEntity(PAYMENT_URL+"/payment/get"+id,CommonResult.class );
//        if(entity.getStatusCode().is2xxSuccessful()){
//            return entity.getBody();
//        }else{
//            return new CommonResult(444,"操作失败");
//        }
//    }

}
