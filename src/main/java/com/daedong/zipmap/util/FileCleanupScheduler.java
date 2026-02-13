package com.daedong.zipmap.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component // 스프링에게 "이 클래스는 관리 대상(Bean)이야"라고 알려줌
public class FileCleanupScheduler {

    @Value("${file.upload-dir}")
    private String uploadDir; // application.properties의 경로 (C:/upload)

    /**
     * [자동 청소 메서드]
     * 매일 새벽 2시에 실행되어, 24시간 이상 지난 temp 파일들을 삭제합니다.
     * * cron 표현식 설명: "초 분 시 일 월 요일"
     * "0 0 2 * * *" -> 매일 02:00:00에 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldTempFiles() {
        log.info("🧹 [스케줄러] 임시 파일(temp) 청소를 시작합니다...");

        File tempDir = new File(uploadDir, "temp");

        // 1. temp 폴더가 아예 없으면 할 일이 없음
        if (!tempDir.exists()) {
            log.info("✨ 청소할 temp 폴더가 없습니다.");
            return;
        }

        // 2. temp 폴더 안의 파일 목록을 가져옴
        File[] files = tempDir.listFiles();
        if (files == null) return;

        // 3. 기준 시간 설정 (현재 시간보다 24시간 전)
        // 즉, 어제 이 시간 이전에 만들어진 파일은 다 지운다!
        long retentionTime = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();

        int deletedCount = 0;
        int failCount = 0;

        for (File file : files) {
            // 파일의 마지막 수정 시간이 기준 시간보다 오래됐으면 삭제 대상
            if (file.lastModified() < retentionTime) {
                if (file.delete()) {
                    deletedCount++;
                } else {
                    failCount++;
                }
            }
        }

        log.info("✨ [스케줄러] 청소 완료!");
        log.info(" - 삭제된 파일: " + deletedCount + "개");
        if (failCount > 0) {
            log.warn(" - 삭제 실패: " + failCount + "개 (파일이 사용 중일 수 있음)");
        }
    }
}