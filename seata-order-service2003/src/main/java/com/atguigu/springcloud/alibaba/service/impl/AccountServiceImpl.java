package com.atguigu.springcloud.alibaba.service.impl;

import com.atguigu.springcloud.alibaba.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER=
            LoggerFactory.getLogger(AccountServiceImpl.class);
    @Resource
    private AccountService accountService;

    @Override
    public void decrease(Long userId, BigDecimal money) {

        LOGGER.info("----------->account-service中扣减账户余额开始");
        accountService.decrease(userId,money);

        LOGGER.info("--------->account-service中扣减账户余额结束");

    }
}
