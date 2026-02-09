package com.daedong.zipmap.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.daedong.zipmap.repository")
public class MyBatisConfig {
}
