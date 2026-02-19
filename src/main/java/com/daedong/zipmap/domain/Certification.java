package com.daedong.zipmap.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 실거주 인증 정보를 담는 객체
 * - 사용자가 올린 파일의 경로와 승인 상태를 관리합니다.
 */
@Data
public class Certification {
    private Long id;              // 인증 PK
    private Long userId;          // 신청자 ID (User 테이블의 id와 매칭)
    private String status;        // 상태 (PENDING, APPROVED, REJECTED)
    private LocalDateTime createdAt; // 신청 일시
}