package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.CrimeStat;
import com.daedong.zipmap.service.CrimeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 안전율 지도용 Controller
 * - AJAX로 안전율 데이터 반환
 */
@RestController
@RequiredArgsConstructor
public class CrimeStatsController {

    private final CrimeStatsService crimeStatsService;

    /**
     * list.html에서 AJAX로 호출
     * @return 서울 구별 안전율 데이터
     */
    @GetMapping("/review/safety-map")
    public List<CrimeStat> getSafetyMapData() {
        return crimeStatsService.getAllSeoulCrimeWithSafety();
    }
}
