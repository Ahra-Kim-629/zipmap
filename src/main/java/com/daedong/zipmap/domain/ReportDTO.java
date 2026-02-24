package com.daedong.zipmap.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Long id;          // 신고 번호 (PK)
    private Long targetId;    // 신고 대상 번호 (게시글 ID 또는 리뷰 ID)
    private String targetType; // 신고 대상 유형 ('POST' 또는 'REVIEW')
    private Long userId;      // 신고자 고유 번호

    private String reasonType; // 신고 사유
    private String content;    // 상세 내용
    private String filePath;   // 첨부 파일 경로

    private String status = "PENDING"; // 처리 상태

    private LocalDateTime createdAt; // 신고 접수 시간

    private String reporterName; // 신고자 이름

    // 기존 코드 호환성을 위해 유지 (삭제 예정)
    public Long getPostId() {
        return targetId;
    }
    public void setPostId(Long postId) {
        this.targetId = postId;
    }
}