package com.daedong.zipmap.controller;

import com.daedong.zipmap.service.CrimeStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrimeStatsController {

    private final CrimeStatsService service;

    public CrimeStatsController(CrimeStatsService service) {
        this.service = service;
    }

    @GetMapping("/crime")
    public String fetchCrimeStats() {
        // 브라우저에서 호출하면 JSON 문자열 그대로 확인 가능
        return service.getCrimeStats();
    }
}
