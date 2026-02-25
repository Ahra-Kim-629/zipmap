package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReportDTO;
import com.daedong.zipmap.domain.ReportStatus;
import com.daedong.zipmap.mapper.ReportMapper;
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

    @Transactional
    public void saveReport(ReportDTO reportDTO, MultipartFile file) {
        // 1. 파일 업로드 처리 (외부 경로 사용으로 로그아웃 방지)
        if (file != null && !file.isEmpty()) {
            reportDTO.setFilePath(uploadFile(file));
        }

        // 2. DB 저장 (여기서 오류가 난다면 매퍼의 #{userId} 확인 필요)
        try {
            reportMapper.insertReport(reportDTO);
        } catch (Exception e) {
            log.error("DB 저장 실패: {}", e.getMessage());
            throw new RuntimeException("DB 저장 에러", e);
        }
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

    @Transactional
    public void toggleReportStatus(Long id) {
        ReportDTO report = reportMapper.selectReportById(id);
        if (report != null) {
            // PENDING -> CONFIRMED, CONFIRMED -> PENDING으로 스위칭
//            String newStatus = "PENDING".equals(report.getStatus()) ? "CONFIRMED" : "PENDING";
            ReportStatus newStatus = (report.getStatus() == ReportStatus.PENDING)
                    ? ReportStatus.CONFIRMED
                    : ReportStatus.PENDING;
            reportMapper.updateStatus(id, newStatus.name());
        }
    }

    public List<ReportDTO> findAllReports(String status) {
        return reportMapper.selectAllReports(status);
    }

    @Transactional
    public void updateReportStatus(Long id, String status) {
        reportMapper.updateStatus(id, status);
    }
    @Transactional
    public void deleteReport(Long id) {
        reportMapper.deleteReport(id);
    }
}