package com.daedong.zipmap.domain.review.entity;

import lombok.Data;


@Data
public class CrimeStats {
    private String region;      // 구 이름 (예: 강남구)
    private int crimeCount;     // 범죄 발생 총 건수
    private String grade;       // 등급 (SAFE, NORMAL, DANGER)
    private double lat;         // 위도
    private double lng;         // 경도
}

