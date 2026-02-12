package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.CrimeStat;
import com.daedong.zipmap.mapper.CrimeStatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 서울 구별 범죄 데이터 Service
 * - DB 저장
 * - 안전율 계산
 */
@Service
@RequiredArgsConstructor
public class CrimeStatsService {

    private final CrimeStatsMapper crimeStatsMapper;

    /**
     * DB에서 서울 구별 범죄 데이터를 조회하고 안전율 계산
     * 안전율 계산: 100 - (해당 구 범죄 건수 / 서울 최대 범죄 건수 * 100)
     * @return 안전율 포함된 CrimeStat 리스트
     */
    public List<CrimeStat> getAllSeoulCrimeWithSafety() {
        List<CrimeStat> list = crimeStatsMapper.getAllSeoulCrime();

        int maxCrime = list.stream()
                .mapToInt(CrimeStat::getCrimeCount)
                .max()
                .orElse(1); // 0으로 나누는 오류 방지

        for (CrimeStat stat : list) {
            double safetyRate = 100.0 - (stat.getCrimeCount() / (double) maxCrime) * 100;
            stat.setSafetyRate(safetyRate);
        }

        return list;
    }

    /**
     * DB에 범죄 데이터 저장
     */
    public void saveCrimeStats(String region, int crimeCount, double lat, double lng) {
        crimeStatsMapper.insertCrime(region, crimeCount, lat, lng);
    }
}
