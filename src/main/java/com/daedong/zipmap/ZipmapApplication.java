package com.daedong.zipmap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ZipmapApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZipmapApplication.class, args);
    }

}
