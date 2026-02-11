package com.daedong.zipmap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.daedong.zipmap.mapper")
public class ZipmapApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZipmapApplication.class, args);
    }

}
