package com.daedong.zipmap.global.config;

import com.daedong.zipmap.global.interceptor.SessionCleanInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir; // application.properties의 경로 (예: C:/upload)

    private final SessionCleanInterceptor sessionCleanInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionCleanInterceptor)
                .addPathPatterns("/**"); // 모든 경로에 대해 인터셉터 작동 (세부 로직은 인터셉터 내부에서 필터링)
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // [기존 코드 삭제]
        // registry.addResourceHandler("/files/notice/**")... (삭제)
        // registry.addResourceHandler("/files/upload/**")... (삭제)

        // [New 통합 설정]
        // "/files/**"로 시작하는 모든 요청을 받습니다. (review, post, notice 다 포함됨)
        registry.addResourceHandler("/files/**")

                // 실제 파일은 uploadDir 폴더("C:/upload/") 바로 아래에서 찾습니다.
                // 예: /files/review/abc.jpg  -->  C:/upload/review/abc.jpg
                .addResourceLocations("file:///" + uploadDir + "/");
    }
}