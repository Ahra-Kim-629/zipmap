package com.daedong.zipmap.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileAttachment {
    private Long id;

    private String targetType;      // 구분 (REVIEW, POST 등)
    private Long targetId;          // 글 번호

    private String filePath;        // 저장된 파일명 (UUID 포함된 이름)
    private Long fileSize;          // 파일 크기

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}