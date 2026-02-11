package com.daedong.zipmap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 브라우저에서 접근할 경로 (예: http://localhost:8080/upload/...)
        registry.addResourceHandler("/upload/**")
                // 2. 실제 파일이 저장된 내 컴퓨터의 물리적 경로
                // 주의: 끝에 반드시 / 를 붙여야 하고, file:/// 로 시작
                .addResourceLocations("file:///C:/upload/");
    }

}
