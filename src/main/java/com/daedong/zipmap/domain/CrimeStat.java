package com.daedong.zipmap.domain;

import lombok.Data;

/**
 * 서울 구별 범죄 및 안전율 정보
 */
@Data
public class CrimeStat {
    private String region;     // 구 이름
    private int crimeCount;    // 범죄 건수
    private double safetyRate; // 안전율 (0~100)
    private double lat;        // 위도
    private double lng;        // 경도
}
