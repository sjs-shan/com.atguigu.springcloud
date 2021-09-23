package com.atguigu.springcloud.alibaba.config;

import jdk.internal.instrumentation.TypeMappings;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.atguigu.springcloud.alibaba.dao")
public class MybatisConfig {

}
