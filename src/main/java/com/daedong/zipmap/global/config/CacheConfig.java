package com.daedong.zipmap.global.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // 사용할 캐시 이름들을 미리 등록해두면 좋대요.
        cacheManager.setCacheNames(List.of("mainNotices", "mainReviewList", "mainPostList", "aiSummaryCache"));

        return cacheManager;
    }
}
