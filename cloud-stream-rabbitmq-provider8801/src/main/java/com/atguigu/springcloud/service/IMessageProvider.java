package com.atguigu.springcloud.service;

public interface IMessageProvider {
    public String send();
    public String send(String msg);
}
