package com.daedong.zipmap.domain.crimestats.controller;

import com.daedong.zipmap.domain.crimestats.entity.CrimeStats;
import com.daedong.zipmap.domain.crimestats.service.CrimeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CrimeStatsController {

    private final CrimeStatsService crimeStatsService;

    /**
     * 안전율 데이터 요청 (AJAX)
     * DB가 아닌 Service의 메모리 데이터를 반환
     */
    @GetMapping("/review/safety-map")
    public List<CrimeStats> getSafetyMapData() {
        return crimeStatsService.getSafetyData();
    }
}