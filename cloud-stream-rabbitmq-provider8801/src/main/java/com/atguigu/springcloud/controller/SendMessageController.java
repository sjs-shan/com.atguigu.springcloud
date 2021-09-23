package com.atguigu.springcloud.controller;

import com.atguigu.springcloud.service.IMessageProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class SendMessageController {
    @Resource
    private IMessageProvider messageProvider;

    @GetMapping(value="/sendMessage")
    public String sendMessage(@RequestParam(required=false) String msg){
        if (msg == null){
            return messageProvider.send();
        }
        return messageProvider.send(msg);
    }
}
