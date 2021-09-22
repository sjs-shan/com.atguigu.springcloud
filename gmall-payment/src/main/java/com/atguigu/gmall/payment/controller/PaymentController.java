package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.PaymentInfo;
import com.atguigu.gmall0218.bean.enums.PaymentStatus;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Reference
    private AlipayClient alipayClient;



    @RequestMapping("index")
    public String index(String orderId, HttpServletRequest request) {

        //选中支付渠道
        // 获取总金额 通过orderId获取订单的总金额
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        // 保存订单id
        request.setAttribute("orderId",orderId);

        // 保存订单总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";

    }

    @RequestMapping("alipay/submit")
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response) {
        /*
        1.保存支付记录 将数据放入数据库
        去重复，对账！ 幂等性=保证每笔交易只能交易一次{第三方交易号 outTradeNo}
        paymentInof
        2.生成二维码
         */

        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();

        //属性赋值
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("给建山买的鞋");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());

        paymentService.savePaymentInfo(paymentInfo);

        //生成二维码
        // 参数做成配置文件进行软编码

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();// 创建一个api对应的request
        //设置同步回调
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);

        // 设置异步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        // 参数 声明一个map来存储参数
        HashMap<String,Object> map=new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());

        // 将封装好的参数传递给支付宝
        alipayRequest.setBizContent(JSON.toJSONString(map));

        String form="";

        try {
            alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");

        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String callbackReturn(){

        return "redirect:"+AlipayConfig.return_order_url;
    }

    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String ,String > paramMap,HttpServletRequest request) {

        boolean flag =false;// 调用sdk验证签名

        try {
            flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (flag) {
            String trade_status = paramMap.get("trade_status");

            String out_trade_no = paramMap.get("out_trade_no");

            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {

                PaymentInfo paymentInfoQuery = new PaymentInfo();

                paymentInfoQuery.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfo=paymentService.getPaymentInfo(paymentInfoQuery);

                if (paymentInfo.getPaymentStatus() ==PaymentStatus.PAID || paymentInfo.getPaymentStatus() ==PaymentStatus.ClOSED) {
                    return "failure";
                }

                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());

                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                return "success";

            }

        }else {
            return "failure";
        }
        return "failure";


    }

    public String refund(String orderId) {
        boolean result=paymentService.refund(orderId);

        return ""+result;

    }
}
