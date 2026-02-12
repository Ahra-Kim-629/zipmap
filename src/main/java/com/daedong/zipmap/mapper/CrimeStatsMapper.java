package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.CrimeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper 인터페이스
 * - DB CRUD 역할
 */
@Mapper
public interface CrimeStatsMapper {

    /**
     * 범죄 데이터 DB 저장
     */
    void insertCrime(@Param("region") String region,
                     @Param("crimeCount") int crimeCount,
                     @Param("lat") double lat,
                     @Param("lng") double lng);

    /**
     * 서울 구별 모든 범죄 데이터 조회
     */
    List<CrimeStat> getAllSeoulCrime();
}
