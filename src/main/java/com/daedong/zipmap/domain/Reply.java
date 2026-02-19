package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Reply {
    private long id;
    private String targetType; // 'POST' or 'REVIEW'
    private long targetId;
    private long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}