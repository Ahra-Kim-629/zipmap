package com.daedong.zipmap.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Long id;          // 신고 번호 (PK)
    private Long postId;      // 신고 대상 게시글 번호 (HTML의 <input name="postId">와 매칭)
    private Long userId;      // 신고자 고유 번호 (Controller에서 setUserId로 설정)

    private String reasonType; // 신고 사유 (HTML의 <select name="reasonType">과 매칭)
    private String content;    // 상세 내용 (HTML의 <textarea name="content">와 매칭)
    private String filePath;   // 서버에 저장된 파일명 (Service에서 설정)

    private String status = "PENDING"; // 기본값: PENDING, 완료 시: COMPLETED

    private LocalDateTime createdAt; // 신고 접수 시간
}