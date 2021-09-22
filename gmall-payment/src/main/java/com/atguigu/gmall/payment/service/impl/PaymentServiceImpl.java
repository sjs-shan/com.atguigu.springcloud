package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.PaymentInfo;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;

    @Reference
    private OrderService orderService;


    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        // select * from paymentInfo where out_trade_no=?
        return paymentInfoMapper.selectOne(paymentInfoQuery);


    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD) {
        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUPD,example);

    }

    @Override
    public boolean refund(String orderId) {

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        HashMap<String, Object> map = new HashMap<>();

        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("refund_amount",orderInfo.getTotalAmount());
        map.put("refund_reason","不买了");

        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeRefundResponse response=null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            return true ;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }
}
