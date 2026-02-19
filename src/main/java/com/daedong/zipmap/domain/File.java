package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class File {
    private long id;
    private String targetType; // 'POST' or 'REVIEW' or 'NOTICE'
    private long targetId;
    private String filePath;
    private long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
