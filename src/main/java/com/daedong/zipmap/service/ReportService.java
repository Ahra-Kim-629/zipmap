package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReportDTO;
import com.daedong.zipmap.domain.ReportStatus;
import com.daedong.zipmap.mapper.ReportMapper;
import com.daedong.zipmap.util.FileUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportMapper reportMapper;
    private final FileUtilService fileUtilService; // 공용 파일 유틸리티 주입

    public boolean hasUserAlreadyReported(Long userId, String targetType, Long targetId) {
        int count = reportMapper.countReportByUserAndTarget(userId, targetType.toUpperCase(), targetId);
        return count > 0;
    }

    @Transactional
    public void saveReport(ReportDTO reportDTO, MultipartFile file) {
        // 1. 파일 업로드 처리 (외부 경로 사용으로 로그아웃 방지)
// 1. 먼저 신고 내역을 DB에 저장합니다. (신고 번호 ID가 생성됨)
        try {
            reportMapper.insertReport(reportDTO);
            Long reportId = reportDTO.getId(); // MyBatis의 useGeneratedKeys="true" 설정 필요


// 2. 첨부 파일이 있다면 공용 파일 시스템(FileUtilService)을 이용해 저장합니다.
            if (file != null && !file.isEmpty()) {
                // (1) 실제 물리 파일 저장 (C:/upload/reports/ 폴더 생성 및 저장)
                String savedPath = fileUtilService.saveFile(file, "reports");
                // (2) 공용 File 도메인 객체 생성 (com.daedong.zipmap.domain.File)
                com.daedong.zipmap.domain.File fileEntity = new com.daedong.zipmap.domain.File();
                fileEntity.setTargetType("REPORT"); // 타겟 타입을 REPORT로 고정
                fileEntity.setTargetId(reportId);   // 방금 생성된 신고 번호와 연결
                fileEntity.setFilePath(savedPath);  // 저장된 상대 경로 (reports/uuid_파일명)
                fileEntity.setFileSize(file.getSize());

                // (3) 공용 file 테이블에 정보 기록
                fileUtilService.saveFileToDB(fileEntity);
            }
        } catch (Exception e) {
            log.error("신고 접수 실패: {}", e.getMessage());
            throw new RuntimeException("신고 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void deleteReport(Long id) {
        // [수정] 삭제 시에도 공용 file 테이블의 데이터와 실제 파일을 먼저 지웁니다.
        List<com.daedong.zipmap.domain.File> files = fileUtilService.getFileList("REPORT", id);
        for (com.daedong.zipmap.domain.File f : files) {
            fileUtilService.deleteFileByPath(f.getFilePath());
        }
        fileUtilService.deleteFilesByTargetTypeAndTargetId("REPORT", id);

        // 최종적으로 신고 내역 삭제
        reportMapper.deleteReport(id);
    }

    private String uploadFile(MultipartFile file) {
        // [수정] 절대 프로젝트 내부(src/main...)를 쓰지 마세요!
        // 윈도우라면 아래처럼 외부 폴더를 지정해야 서버가 안 꺼집니다.
        String uploadPath = "C:/zipmap_storage/reports/";

        File folder = new File(uploadPath);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                log.error("폴더 생성 실패: {}", uploadPath);
            }
        }

        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + file.getOriginalFilename();

        // Path 객체를 사용하여 경로 문제 방지
        Path savePath = Paths.get(uploadPath, savedFileName);

        try {
            // transferTo에 File 객체 대신 Path를 File로 변환하여 전달하거나, 
            // 절대 경로를 가진 File 객체를 사용
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장 에러", e);
        }
        return savedFileName;
    }

    public List<ReportDTO> findAllReports(String status) {
        return reportMapper.selectAllReports(status);
    }

    // 2. [추가] 인자 없는 메서드 (관리자 목록 첫 진입용)
    public List<ReportDTO> findAllReports() {
        return reportMapper.selectAllReports(null);
        // null을 보내면 MyBatis XML의 <if test="status != null"> 조건에 걸리지 않아 전체가 조회됩니다.
    }

    @Transactional
    public void updateReportStatus(Long id, String status) {
        reportMapper.updateStatus(id, status);
    }

    public int countPendingReports() {
        return reportMapper.countPendingReports();
    }

    // ReportService.java 내부에 추가
    @Transactional
    public void toggleReportStatus(Long id) {
        // 1. 신고 내역 상세 조회 (현재 상태를 알기 위해)
        ReportDTO report = reportMapper.selectReportById(id);

        if (report != null) {
            // 2. 현재 상태가 PENDING이면 CONFIRMED로, 아니면 PENDING으로 반전
            // (DTO에서 사용하시는 ReportStatus ENUM 활용)
            ReportStatus newStatus = (report.getReportStatus() == ReportStatus.PENDING)
                    ? ReportStatus.CONFIRMED
                    : ReportStatus.PENDING;

            // 3. 변경된 상태값(문자열)으로 DB 업데이트
            reportMapper.updateStatus(id, newStatus.name());
            log.info("신고 상태 변경 완료 - ID: {}, New Status: {}", id, newStatus);
        }
    }
}