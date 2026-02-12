package com.daedong.zipmap.domain;

import lombok.Data;

/**
 * 서울 구별 범죄 통계 데이터를 담는 객체 (DTO)
 * - 공공데이터 API 결과를 가공하여 저장
 */
@Data
public class CrimeStats {
    private String region;      // 구 이름 (예: 강남구, 종로구)
    private int crimeCount;     // 총 범죄 발생 건수
    private String grade;       // 안전 등급 (SAFE, NORMAL, DANGER) - 화면 표시용
    private double lat;         // 위도 (카카오맵 마커용)
    private double lng;         // 경도 (카카오맵 마커용)
}