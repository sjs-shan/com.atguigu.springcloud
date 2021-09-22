package com.atguigu.gmall0218.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.service.CartService;
import com.atguigu.gmall0218.service.ManageService;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.UserService;
import com.atguigu.gmall0218.util.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    //    @Autowired
    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @Reference
    private ManageService manageService;

    //    @RequestMapping("trade")
    //    public String trade(){
    //        // 返回一个视图名称叫index.html
    //        return "index";
    //    }
    @RequestMapping("trade")
    @ResponseBody // 第一个返回json 字符串，fastJson.jar 第二直接将数据显示到页面！
    public String trade(HttpServletRequest request) {
        // http://localhost:8081?userId=1
        String userId = (String) request.getAttribute("userId");
        // 返回一个视图名称叫index.html
        // return userService.getUserAddressList(userId)
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList", userAddressList);

        // 展示送货清单
        // 数据来源：勾选的购物车 user:userId:checked
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);

        // 声明一个集合来存储订单明细
        List<OrderDetail> orderDetailArrayList = new ArrayList<>();

        // 将集合数据赋予orderDetail
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());

            orderDetailArrayList.add(orderDetail);
        }
        // 总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailArrayList);
        // 调用计算总金额的方法{tototalAmont}
        orderInfo.sumTotalAmount();

        request.setAttribute("totalAmont",orderInfo.getTotalAmount());

        // 保存送货清单集合
        request.setAttribute("orderDetailArrayList",orderDetailArrayList);

        String tradeNo=orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);


        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(HttpServletRequest request, OrderInfo orderInfo) {

        String userId = (String) request.getAttribute("userId");
        // orderInfo总缺少一个userId
        orderInfo.setUserId(userId);

        // 判断是否重复提交
        // 先获取页面的流水号
        String tradeCodeNo = request.getParameter("tradeNo");
        // 调用方法比较
        boolean result=orderService.checkTradeCode(userId,tradeCodeNo);
        // 是重复提交
        if (!result) {
            request.setAttribute("errMsg","订单已提交，不能重复提交");
            return "tradeFail";
        }
        // 验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean flag=orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            if (!flag) {
                request.setAttribute("errMsg",orderDetail.getSkuNum()+"商品库存不足");
                return "tradeFail";
            }
            // 获取skuInfo对象
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            int res = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());

            if (res!=0) {
                request.setAttribute("errMsg",orderDetail.getSkuNum()+"价格不匹配");
                cartService.loadCartCach(userId);
                return "tradeFail";

            }

        }


        //调用服务层
        String orderId=orderService.saveOrder(orderInfo);
        // 删除流水号
        orderService.delTradeNo(userId);

        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }

}
