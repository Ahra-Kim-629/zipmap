package com.daedong.zipmap.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CrimeStatsService {

    private final RestTemplate restTemplate = new RestTemplate();

    // application-secret.properties에서 값 읽기
    @Value("${crime.api.key}")
    private String apiKey;

    @Value("${crime.api.url}")
    private String baseUrl;

    public String getCrimeStats() {
        // URL 구성: Base URL + 인증키 + 페이지/갯수
        String url = baseUrl + "?serviceKey=" + apiKey + "&page=1&perPage=10";
        // API 호출, JSON 그대로 문자열로 반환
        return restTemplate.getForObject(url, String.class);
    }
}
