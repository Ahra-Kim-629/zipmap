package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Reaction {
    private Long id;
    private String targetType; // REVIEW, POST 등
    private Long targetId;     // 대상의 PK 번호
    private Long userId;       // 좋아요 누른 사용자 ID
    private int type;          // 1: 좋아요, -1: 싫어요
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
