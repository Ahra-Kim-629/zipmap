package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostFile {
    private Long id;
    private Long postId;
    private String filePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
