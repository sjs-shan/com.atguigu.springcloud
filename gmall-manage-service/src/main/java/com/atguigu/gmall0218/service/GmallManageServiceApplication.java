package com.atguigu.gmall0218.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com/atguigu/gmall0218/service/mapper")
@EnableTransactionManagement
public class GmallManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallManageServiceApplication.class, args);
    }

}
