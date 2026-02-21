package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Reply {
    private Long id;
    private String targetType; // REVIEW, POST 등
    private Long targetId;     // 대상의 PK 번호
    private Long userId;       // 댓글 작성자 ID
    private String content;    // 댓글 내용
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
