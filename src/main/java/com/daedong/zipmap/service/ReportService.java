package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReportDTO;
import com.daedong.zipmap.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File; // java.io.File을 써야 합니다! (domain.File 삭제)
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportMapper reportMapper;

    @Transactional
    public void saveReport(ReportDTO reportDTO, MultipartFile file) {
        // 💡 로그를 찍어 실제 데이터가 서비스까지 도달했는지 확인합니다.
        log.info("### DB 저장 직전 데이터 확인: postId={}, userId={}", reportDTO.getPostId(), reportDTO.getUserId());

        if (file != null && !file.isEmpty()) {
            reportDTO.setFilePath(uploadFile(file));
        }

        try {
            reportMapper.insertReport(reportDTO);
            log.info("### 쿼리 실행 직후 - 성공 메시지가 보이나요?");
            log.info("### DB 저장 쿼리 실행 성공!");
            if (file != null && !file.isEmpty()) {
                reportDTO.setFilePath(uploadFile(file));
            }
        } catch (Exception e) {
            log.error("### DB 저장 실패 원인: ", e); // 에러의 상세 원인(Stacktrace)을 찍습니다.
            throw new RuntimeException("DB 저장 에러", e);
        }
    }

    private String uploadFile(MultipartFile file) {
        // [수정] 절대 프로젝트 내부(src/main...)를 쓰지 마세요!
        // 윈도우라면 아래처럼 외부 폴더를 지정해야 서버가 안 꺼집니다.
        String uploadPath = "C:/zipmap_storage/reports/";

        java.io.File folder = new java.io.File(uploadPath);
        if (!folder.exists()) folder.mkdirs();

        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + file.getOriginalFilename();

        try {
            file.transferTo(new java.io.File(uploadPath, savedFileName));
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장 에러");
        }
        return savedFileName;
    }

    public List<ReportDTO> findAllReports() {
        return reportMapper.selectAllReports();
    }
}