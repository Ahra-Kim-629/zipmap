package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.ReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {
    // 1. 신고 접수 (사용자용)
    void insertReport(ReportDTO reportDTO);

    // 2. 신고 목록 조회 (관리자용 - 미리 만들어둡니다)
    List<ReportDTO> selectAllReports(@Param("status") String status);
    // 3. 신고 상세 조회 (관리자용)
    ReportDTO selectReportById(Long id);
    void deleteReport(Long id);
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}